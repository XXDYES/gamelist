package org.example.gamelist.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.gamelist.client.AiClient;
import org.example.gamelist.common.UserContext;
import org.example.gamelist.config.AiConfig;
import org.example.gamelist.dto.GameInfoDTO;
import org.example.gamelist.exception.AiServiceException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DeepSeekClientImpl implements AiClient {

    private final WebClient webClient;
    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeepSeekClientImpl(WebClient aiWebClient, AiConfig aiConfig) {
        this.webClient = aiWebClient;
        this.aiConfig = aiConfig;
        log.info("AI 模型：{} | maxTokens={} | temperature={}",
                aiConfig.getModel(), aiConfig.getMaxTokens(), aiConfig.getTemperature());
    }

    @Override
    public GameInfoDTO getGameInfo(String gameName) {
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new AiServiceException("游戏名称不能为空");
        }
        long start = System.currentTimeMillis();
        try {
            GameInfoDTO dto = callResponsesApi(gameName.trim()).block();
            log.info("{} {} AI 调用耗时 {} ms", UserContext.getCurrentUsername(), gameName,
                    System.currentTimeMillis() - start);
            return dto;
        } catch (AiServiceException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("AI 接口返回 {}: {} | game={}", e.getStatusCode(), e.getResponseBodyAsString(), gameName);
            throw new AiServiceException("AI 服务调用失败，状态码 " + e.getStatusCode());
        } catch (Exception e) {
            log.error("调用 DeepSeek Responses API 失败 | game={}", gameName, e);
            throw new AiServiceException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    private Mono<GameInfoDTO> callResponsesApi(String gameName) {
        return webClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildRequestBody(gameName))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> extractOutputText(json, gameName))
                .map(text -> parseAndValidate(text, gameName));
    }

    private Map<String, Object> buildRequestBody(String gameName) {
        Map<String, Object> requestBody = new HashMap<>();
        // 模型从 application.yml 的 ai.api.model 读取，换模型不用改代码
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("temperature", aiConfig.getTemperature());
        requestBody.put("max_output_tokens", aiConfig.getMaxTokens());
        // 非流式：不设置 stream，默认 false
        // deepseek官方配置（官方端点只认 deepseek-flash / deepseek-v4-pro）
//        requestBody.put("reasoning", Map.of("effort", "none"));
//        requestBody.put("tools", List.of(Map.of("type", "web_search")));

        // 千问AI平台配置
        requestBody.put("enable_thinking", false);
        requestBody.put("tools", List.of(Map.of("type", "web_search")));
        requestBody.put("max_tool_calls", 2);

        // JSON 输出：text.format 是两层，不能写成 text.type
        requestBody.put("text", Map.of("format", Map.of("type", "json_object")));

        requestBody.put("instructions", "你是游戏信息助手，只输出 JSON，不要任何解释或代码块。" +
                "JSON 字符串内不要出现英文双引号，需要引号时用「」或《》。" +
                "只要输入是游戏，无论你是否熟悉，都必须先调用一次 web_search 联网核对后再作答，" +
                "凭记忆直接作答视为不合格。搜索后仍查不到的字段才填\"暂无\"，绝不编造。" +
                "若输入明显不是游戏（食物、物品、人名、地名、句子、乱码等），不要搜索，" +
                "直接返回无关格式：name 原样返回输入，其余字段全为空字符串，cover 为 null。" +
                "注意：没听过或查不到资料的游戏仍算游戏，必须走正常格式。");
        requestBody.put("input", buildPrompt(gameName));
        return requestBody;
    }

    private String extractOutputText(JsonNode response, String gameName) {
        JsonNode output = response.path("output");
        if (output.isMissingNode() || !output.isArray()) {
            throw new AiServiceException("响应中缺少 output 字段");
        }

        String status = response.path("status").asText();
        if ("failed".equals(status)) {
            throw new AiServiceException("AI 返回失败：" + response.path("error").path("message").asText());
        }
        if ("incomplete".equals(status)) {
            throw new AiServiceException("AI 返回被截断：" + response.path("incomplete_details").path("reason").asText());
        }

        JsonNode usage = response.path("usage");
        log.info("AI 用量：input={} cached={} output={} reasoning={} | game={}",
                usage.path("input_tokens").asInt(),
                usage.path("input_tokens_details").path("cached_tokens").asInt(),
                usage.path("output_tokens").asInt(),
                usage.path("output_tokens_details").path("reasoning_tokens").asInt(),
                gameName);

        // 统计 output item 类型，确认 web_search 到底执行了几次
        List<String> itemTypes = new ArrayList<>();
        int searchCalls = 0;
        for (JsonNode item : output) {
            String type = item.path("type").asText();
            itemTypes.add(type);
            if ("web_search_call".equals(type)) {
                searchCalls++;
            }
        }
        log.info("output items: {} ，其中 web_search_call {} 次 | game={}", itemTypes, searchCalls, gameName);
        if (searchCalls > 2) {
            log.warn("本次联网搜索 {} 次，超过预期阈值 2 次，请留意成本 | game={}", searchCalls, gameName);
        }

        for (JsonNode item : output) {
            if ("message".equals(item.path("type").asText())) {
                JsonNode content = item.path("content");
                if (content.isArray()) {
                    for (JsonNode part : content) {
                        if ("output_text".equals(part.path("type").asText())) {
                            return part.path("text").asText();
                        }
                    }
                }
            }
        }
        throw new AiServiceException("响应中未找到 output_text");
    }

    private GameInfoDTO parseAndValidate(String jsonContent, String gameName) {
        if (jsonContent == null || jsonContent.isEmpty()) {
            throw new AiServiceException("AI 返回内容为空");
        }
        log.info("AI 返回的完整 JSON: {} | game={}", jsonContent, gameName);
        try {
            return objectMapper.readValue(jsonContent, GameInfoDTO.class);
        } catch (Exception e) {
            throw new AiServiceException("AI 返回的 JSON 解析失败: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String gameName) {
        return String.format(
                "查询游戏《%s》，只返回下面这个 JSON（字段名不要改）：\n" +
                        "{\"name\":\"游戏官方中文名；无法确认就原样返回输入\"," +
                        "\"company\":\"开发商，只填一个，优先中文\"," +
                        "\"platform\":\"实际登陆的平台，从 PC/PS/NS/XBOX/移动端 中选，用/分隔；全部登陆才写 全平台\"," +
                        "\"type\":\"游戏类型，如 开放世界、JRPG、AVG\"," +
                        "\"cover\":null," +
                        "\"info\":\"简介，100字左右，不超过150字，介绍游戏内容\"," +
                        "\"price\":\"该游戏官方商店的售价，只填价格本身（如 免费、¥298、HK$368、$79.99），" +
                        "有 PC 版优先 PC，没有 PC 版用主机商店的售价，不确定填 暂无\"," +
                        "\"mcRating\":\"Metacritic 媒体均分纯数字，如 90，取 PC 版\"," +
                        "\"releaseDate\":\"发售日期，格式 YYYY-MM-DD；未发售或不确定填 暂无\"}\n" +
                        "输入无关时的返回格式（只在输入明显不是游戏时使用）：\n" +
                        "{\"name\":\"<原样返回输入>\",\"company\":\"\",\"platform\":\"\",\"type\":\"\",\"cover\":null," +
                        "\"info\":\"\",\"price\":\"\",\"mcRating\":\"\",\"releaseDate\":\"\"}\n" +
                        "判定示例：\"红烧肉的做法\"、\"asdfghjkl\" → 无关格式；\"剑星\"、\"星海旅人\"（不认识的游戏）→ 正常格式\n" +
                "联网规则：\n" +
                "1. 先判断是否与游戏相关：无关就不要搜索，直接返回上面的无关格式。\n" +
                "2. 是游戏就必须先搜一次（无论是否熟悉、是否已发售），没搜就回答不合格。\n" +
                "3. 这一次搜索的检索词全部用英文（游戏名用它的英文名），" +
                "必须同时包含「游戏英文名 Metacritic」和「游戏英文名 Steam price」，" +
                "只搜这一次，搜完立即输出 JSON，禁止打开网页。\n" +
                "4. 填 暂无 之前必须逐条看一遍搜索结果：只要结果里出现售价或 Metascore 数字就必须填上，" +
                "不许因为要多读一遍就填 暂无，绝不编造。",
                gameName);
    }
}
