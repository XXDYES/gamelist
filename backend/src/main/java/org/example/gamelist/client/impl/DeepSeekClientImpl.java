package org.example.gamelist.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.gamelist.client.AiClient;
import org.example.gamelist.config.AiConfig;
import org.example.gamelist.dto.GameInfoDTO;
import org.example.gamelist.exception.AiServiceException;
import org.example.gamelist.util.AiResponseParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DeepSeekClientImpl implements AiClient {

    private final RestTemplate restTemplate;
    private final AiConfig aiConfig;
    private final AiResponseParser responseParser;

    public DeepSeekClientImpl(RestTemplate restTemplate, AiConfig aiConfig, AiResponseParser responseParser) {
        this.restTemplate = restTemplate;
        this.aiConfig = aiConfig;
        this.responseParser = responseParser;
    }

    @Override
    public GameInfoDTO getGameInfo(String gameName) {
        try {
            // 1. 构造请求体（DeepSeek 兼容 OpenAI 格式）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.getModel());
            requestBody.put("temperature", aiConfig.getTemperature());
            requestBody.put("max_tokens", aiConfig.getMaxTokens());

            // ⭐ 启用 JSON Output
            Map<String, String> responseFormat = new HashMap<>();
            responseFormat.put("type", "json_object");
            requestBody.put("response_format", responseFormat);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system",
                    "content", "你是一个游戏信息助手。请严格按照JSON格式返回结果，不要包含任何其他文字或解释。"));
            messages.add(Map.of("role", "user", "content", buildPrompt(gameName)));
            requestBody.put("messages", messages);

            // 2. 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiConfig.getKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 3. 调用 DeepSeek API
            Map<String, Object> response = restTemplate.postForObject(
                    aiConfig.getUrl(), entity, Map.class);

            if (response == null) {
                throw new AiServiceException("DeepSeek API 返回为空");
            }

            // 4. 提取 content
            String content = extractContentFromResponse(response);
            if (content == null) {
                throw new AiServiceException("DeepSeek 返回内容为空");
            }
            log.info("AI 返回的原始 content: {}", content);

            // 5. 解析 JSON
            return responseParser.parse(content);

        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            throw new AiServiceException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContentFromResponse(Map<String, Object> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> choice = choices.get(0);
            Map<String, String> message = (Map<String, String>) choice.get("message");
            if (message != null) {
                return message.get("content");
            }
        }
        return null;
    }

    private String buildPrompt(String gameName) {
        return String.format(
                "根据游戏名称 \"%s\" 返回以下 JSON（不要任何额外文字）：\n" +
                        "{\"name\":\"游戏名称，用户可能输入缩写或绰号，返回官方名称,优先中文名称\"," +
                        "\"company\":\"开发商，优先中文名称\"," +
                        "\"platform\":\"平台，IOS和安卓统称为移动端，电脑端统称PC，switch1和2统称NS,PlayStation统称PS,XBOX统称XBOX，平台之间以/分隔，如PC/移动端\",\n" +
                        "\"type\":\"游戏类型，参考官方的定义，例如原神为开放世界，大写字母优先，如JRPG,AVG等\"," +
                        "\"cover\":\"默认返回null\"," +
                        "\"info\":\"简介(250字左右)，游戏内容介绍占比多一点\"," +
                        "\"price\":\"有上架STEAM平台的优先返回对应国服的实时价格，如果是主机独占游戏，则以对应的港服价格为准，免费游戏则返回免费，返回的金额要有单位，如HK$368，￥298\"," +
                        "\"mc_rating\":\"返回Metacritic网站的对应评分数字，但是String格式，如果没找到就返回暂无\"," +
                        "\"release_date\":\"返回游戏的发售日期，参考格式：2019-10-31，如果还未发售或者没找到，就返回暂无\"}",
                gameName);
    }
}