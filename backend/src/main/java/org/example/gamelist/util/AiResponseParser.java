package org.example.gamelist.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.gamelist.dto.GameInfoDTO;
import org.example.gamelist.exception.AiServiceException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AiResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GameInfoDTO parse(String aiContent) {
        if (aiContent == null || aiContent.trim().isEmpty()) {
            throw new AiServiceException("AI 返回内容为空");
        }

        log.debug("AI 原始返回: {}", aiContent);
        String json = extractJson(aiContent);

        try {
            return objectMapper.readValue(json, GameInfoDTO.class);
        } catch (Exception e) {
            log.error("JSON 解析失败: {}", json, e);
            throw new AiServiceException("AI 返回的 JSON 格式不正确");
        }
    }

    private String extractJson(String content) {
        content = content.trim();

        // 1. 尝试匹配 Markdown 代码块 ```json ... ```
        Matcher m = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```").matcher(content);
        if (m.find() && isValidJson(m.group(1).trim())) {
            return m.group(1).trim();
        }

        // 2. 尝试匹配纯 JSON 对象 {...}
        m = Pattern.compile("\\{[\\s\\S]*\\}").matcher(content);
        if (m.find() && isValidJson(m.group().trim())) {
            return m.group().trim();
        }

        // 3. 直接尝试解析整个内容
        if (isValidJson(content)) {
            return content;
        }

        throw new AiServiceException("无法从 AI 返回中提取有效 JSON");
    }

    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}