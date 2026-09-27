package org.example.gamelist.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.gamelist.vo.HistoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ChatHistoryStore {

    private static final String KEY_PREFIX = "chat:history:";

    /** 最多保留的 item 条数（20 条 = 10 轮对话） */
    private static final int MAX_ITEMS = 20;

    /** 上下文有效期，每次写入都续期，避免对话进行到一半过期 */
    private static final Duration TTL = Duration.ofHours(24);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /** 追加一条用户消息 */
    public void appendUser(String clientId, String text) {
        append(clientId, buildItem("user", text));
    }

    /** 追加一条助手回复（必须传完整回答，不能传流式分片） */
    public void appendAssistant(String clientId, String text) {
        append(clientId, buildItem("assistant", text));
    }

    /** 读取完整上下文，返回结果可直接作为 /responses 的 input */
    public List<Map<String, Object>> load(String clientId) {
        List<String> raw = stringRedisTemplate.opsForList().range(key(clientId), 0, -1);
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> items = new ArrayList<>(raw.size());
        for (String json : raw) {
            try {
                items.add(objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
                }));
            } catch (Exception e) {
                // 单条脏数据跳过，不能让整轮对话挂掉
                log.warn("对话历史反序列化失败，已跳过该条 | clientId: {} | 内容: {}", clientId, json, e);
            }
        }
        return items;
    }

    public void clear(String clientId) {
        stringRedisTemplate.delete(key(clientId));
    }

    public HistoryVO toHistoryVO(Map<String, Object> item) {
        Object role = item.get("role");
        Object content = item.get("content");
        if (role == null || !(content instanceof List<?> parts) || parts.isEmpty()) {
            return null;
        }
        if (!(parts.get(0) instanceof Map<?, ?> part) || part.get("text") == null) {
            return null;
        }
        HistoryVO vo = new HistoryVO();
        vo.setRole(String.valueOf(role));
        vo.setContent(String.valueOf(part.get("text")));
        return vo;
    }

    private void append(String clientId, Map<String, Object> item) {
        try {
            String json = objectMapper.writeValueAsString(item);
            stringRedisTemplate.opsForList().rightPush(key(clientId), json);
            stringRedisTemplate.opsForList().trim(key(clientId), -MAX_ITEMS, -1);
            stringRedisTemplate.expire(key(clientId), TTL);
        } catch (Exception e) {
            // 历史写失败不影响本次对话
            log.warn("写入对话历史失败 | clientId: {}", clientId, e);
        }
    }

    private Map<String, Object> buildItem(String role, String text) {
        // user 必须配 input_text，assistant 必须配 output_text，混用会被网关拒绝
        String contentType = "assistant".equals(role) ? "output_text" : "input_text";
        return Map.of(
                "type", "message",
                "role", role,
                "content", List.of(Map.of("type", contentType, "text", text))
        );
    }

    private String key(String clientId) {
        return KEY_PREFIX + clientId;
    }
}
