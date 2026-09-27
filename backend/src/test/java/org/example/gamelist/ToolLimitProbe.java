package org.example.gamelist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 探测 max_tool_calls 是否被服务端真正执行。
 * 同一个提示词分别在不设上限 / 设上限 2 的情况下各跑一次，对比 web_search_call 的个数。
 */
public class ToolLimitProbe {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String yml = Files.readString(Path.of("backend/src/main/resources/application.yml"), StandardCharsets.UTF_8);
        String baseUrl = first(yml, "(?m)^\\s*base-url:\\s*\"?([^\"\\r\\n]+?)\"?\\s*$");
        String key = first(yml, "(?m)^\\s*key:\\s*\"?([^\"\\r\\n]+?)\"?\\s*$");
        String model = first(yml, "(?m)^\\s*model:\\s*\"?([^\"\\r\\n]+?)\"?\\s*$");
        System.out.println("base-url = " + baseUrl);
        System.out.println("model    = " + model);
        System.out.println("key      = " + (key == null ? "null" : key.substring(0, Math.min(10, key.length())) + "***"));

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .build();

        if (args.length >= 2) {
            // 单独跑一个场景：java ... ToolLimitProbe 1 false
            run(client, model, Integer.parseInt(args[0]), Boolean.parseBoolean(args[1]));
        } else {
            run(client, model, null, null);
            run(client, model, 2, null);
        }
    }

    private static void run(WebClient client, String model, Integer maxToolCalls, Boolean parallel) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.3);
        body.put("max_output_tokens", 6000);
        body.put("reasoning", Map.of("effort", "none"));
        body.put("tools", List.of(Map.of("type", "web_search")));
        body.put("text", Map.of("format", Map.of("type", "json_object")));
        if (maxToolCalls != null) {
            body.put("max_tool_calls", maxToolCalls);
        }
        if (parallel != null) {
            body.put("parallel_tool_calls", parallel);
        }
        body.put("input", "查询游戏《剑星》，必须用 web_search 分别查证它的 Metacritic 分数、售价、发售日期，"
                + "每个字段单独搜索一次，最后只返回 JSON。");

        long start = System.currentTimeMillis();
        try {
            String json = client.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(180));
            JsonNode root = MAPPER.readTree(json);
            int calls = 0;
            StringBuilder types = new StringBuilder();
            for (JsonNode item : root.path("output")) {
                String t = item.path("type").asText();
                types.append(t).append(" ");
                if ("web_search_call".equals(t)) calls++;
            }
            System.out.printf("max_tool_calls=%-4s parallel=%-4s -> 200  搜索次数=%d  耗时=%d ms%n   output items: %s%n",
                    String.valueOf(maxToolCalls), String.valueOf(parallel), calls,
                    System.currentTimeMillis() - start, types.toString().trim());
        } catch (Exception e) {
            System.out.printf("max_tool_calls=%-4s parallel=%-4s -> 失败: %s%n",
                    String.valueOf(maxToolCalls), String.valueOf(parallel), e.getMessage());
        }
    }

    private static String first(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }
}
