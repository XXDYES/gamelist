package org.example.gamelist.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.gamelist.client.AiClient;
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

/**
 * 调用 DeepSeek 的 Responses API（POST /responses，OpenAI 兼容格式）。
 * <p>
 * 启用内置 web_search 工具并关闭思考模式。每次响应会在日志里报告
 * output item 类型、web_search_call 次数、引用来源、耗时与 token 用量。
 */
@Slf4j
@Component
public class DeepSeekClientImpl implements AiClient {

    private final WebClient webClient;
    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeepSeekClientImpl(WebClient aiWebClient, AiConfig aiConfig) {
        this.webClient = aiWebClient;
        this.aiConfig = aiConfig;
    }

    @Override
    public GameInfoDTO getGameInfo(String gameName) {
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new AiServiceException("游戏名称不能为空");
        }
        long start = System.currentTimeMillis();
        try {
            GameInfoDTO dto = callResponsesApi(gameName.trim()).block();
            log.info("AI 调用耗时 {} ms", System.currentTimeMillis() - start);
            return dto;
        } catch (AiServiceException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("AI 接口返回 {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException("AI 服务调用失败，状态码 " + e.getStatusCode());
        } catch (Exception e) {
            log.error("调用 DeepSeek Responses API 失败", e);
            throw new AiServiceException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    /** 构造请求并发起调用，返回 Mono 由调用方 block() */
    private Mono<GameInfoDTO> callResponsesApi(String gameName) {
        return webClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildRequestBody(gameName))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::extractOutputText)
                .map(this::parseAndValidate);
    }

    private Map<String, Object> buildRequestBody(String gameName) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("temperature", aiConfig.getTemperature());
        requestBody.put("max_output_tokens", aiConfig.getMaxTokens());
        // 非流式：不设置 stream，默认 false
        // 关闭推理，加速响应
        requestBody.put("reasoning", Map.of("effort", "none"));
        // ⭐ 启用内置 web_search 工具（服务端执行）
        requestBody.put("tools", List.of(Map.of("type", "web_search")));
        // JSON 输出：text.format 是两层，不能写成 text.type
        requestBody.put("text", Map.of("format", Map.of("type", "json_object")));

        requestBody.put("instructions", "你是游戏信息助手。只输出 JSON，不要任何解释或代码块。" +
                "JSON 字符串内部禁止使用英文双引号，需要引用名称时用中文引号「」或书名号《》。" +
                "先凭知识作答；但只要 mcRating、price、releaseDate 中有你不确定的，" +
                "就必须联网搜索查准，不要直接填\"暂无\"。" +
                "搜索后仍查不到的字段才填\"暂无\"。绝不编造。");
        requestBody.put("input", buildPrompt(gameName));
        return requestBody;
    }

    /**
     * 从 Responses API 完整响应中提取 output_text。
     * 响应结构：output 数组 → 找到 type=message 的项 → content 数组 → 找到 type=output_text 的项 → text 字段
     */
    private String extractOutputText(JsonNode response) {
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
        log.info("AI 用量：input={} cached={} output={} reasoning={}",
                usage.path("input_tokens").asInt(),
                usage.path("input_tokens_details").path("cached_tokens").asInt(),
                usage.path("output_tokens").asInt(),
                usage.path("output_tokens_details").path("reasoning_tokens").asInt());

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
        log.info("output items: {} ，其中 web_search_call {} 次", itemTypes, searchCalls);

        for (JsonNode item : output) {
            if ("message".equals(item.path("type").asText())) {
                JsonNode content = item.path("content");
                if (content.isArray()) {
                    for (JsonNode part : content) {
                        if ("output_text".equals(part.path("type").asText())) {
                            logCitations(part.path("annotations"));
                            return part.path("text").asText();
                        }
                    }
                }
            }
        }
        throw new AiServiceException("响应中未找到 output_text");
    }

    /** 记录 output_text 带回的引用来源（搜索命中的网页） */
    private void logCitations(JsonNode annotations) {
        if (annotations == null || !annotations.isArray() || annotations.isEmpty()) {
            return;
        }
        for (JsonNode annotation : annotations) {
            if ("url_citation".equals(annotation.path("type").asText())) {
                log.info("引用来源：{} {}", annotation.path("title").asText(), annotation.path("url").asText());
            }
        }
    }

    private GameInfoDTO parseAndValidate(String jsonContent) {
        if (jsonContent == null || jsonContent.isEmpty()) {
            throw new AiServiceException("AI 返回内容为空");
        }
        log.info("AI 返回的完整 JSON: {}", jsonContent);
        try {
            return objectMapper.readValue(jsonContent, GameInfoDTO.class);
        } catch (Exception e) {
            throw new AiServiceException("AI 返回的 JSON 解析失败: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String gameName) {
        return String.format(
                "查询游戏《%s》，只返回下面这个 JSON（字段名不要改）：\n" +
                        "{\"name\":\"游戏官方中文名；无法确认时原样返回输入的名称，不要中英文混搭\"," +
                        "\"company\":\"开发商，只返回一个，优先中文名称，不要中英文混搭\"," +
                        "\"platform\":\"该游戏实际登陆的平台，从 PC/PS/NS/XBOX/移动端 中选，用/分隔；只有全部登陆才写 全平台\"," +
                        "\"type\":\"游戏类型，如 开放世界、JRPG、AVG\"," +
                        "\"cover\":null," +
                        "\"info\":\"简介，100字左右，别超过150字，以游戏内容介绍为主\"," +
                        "\"price\":\"如 免费、¥298、HK$368；不确定填 暂无，多平台优先PC端\"," +
                        "\"mcRating\":\"Metacritic 媒体均分（Metascore）纯数字，如 90。取 PC 版分数即可\"," +
                        "\"releaseDate\":\"发售日期，格式 YYYY-MM-DD；未发售或不确定填 暂无\"}\n" +
                        "联网规则：\n" +
                        "1. name/company/platform/type/info 你有把握就直接写，不必联网。\n" +
                        "2. 只要 mcRating、price、releaseDate 里有一项不确定，就用游戏官方英文名调用 web_search 查询。\n" +
                        "3. 只用搜索结果的摘要作答，不要打开网页，一次搜索就能拿到 Metascore 和售价。\n" +
                        "4. 总共最多搜索 2 次。查证后仍拿不准的字段填 暂无，不要编造。",
                gameName);
    }
}
