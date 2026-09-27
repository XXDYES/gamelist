package org.example.gamelist;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/** 探测 /responses 的 input 字段：单个对象 vs 数组，哪种能被接受 */
public class InputFormatProbe {

    public static void main(String[] args) {
        WebClient client = WebClient.builder()
                .baseUrl("https://api.deepseek.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + System.getenv("APIKEY_GAME"))
                .build();

        Map<String, Object> item = Map.of(
                "type", "message",
                "role", "user",
                "content", List.of(Map.of("type", "input_text", "text", "你好"))
        );

        probe(client, "input = 单个对象 buildItem(msg)", item);
        probe(client, "input = 数组 List.of(buildItem(msg))", List.of(item));
    }

    private static void probe(WebClient client, String label, Object input) {
        Map<String, Object> body = Map.of(
                "model", "deepseek-v4-flash",
                "stream", true,
                "input", input,
                "reasoning", Map.of("effort", "none"),
                "max_output_tokens", 50
        );
        try {
            String first = client.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .blockFirst();
            String brief = first == null ? "null" : first.substring(0, Math.min(100, first.length()));
            System.out.println("[" + label + "]  ->  成功（200），首个事件: " + brief);
        } catch (Exception e) {
            System.out.println("[" + label + "]  ->  失败: " + e.getMessage());
        }
    }
}
