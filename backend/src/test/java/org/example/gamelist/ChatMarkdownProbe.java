package org.example.gamelist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gamelist.client.impl.ChatClientImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证对话提示词里的「禁止 Markdown」有没有被模型遵守。
 * 提示词直接反射 ChatClientImpl.CHAT_INSTRUCTIONS，保证测的就是线上那份。
 */
public class ChatMarkdownProbe {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String MODEL = "deepseek-v4-pro";
    private static final String[] PROMPTS = {
            "今年刚发售的游戏有哪些",
            "最近有什么值得玩的新游戏",
            "介绍一下原神"
    };

    public static void main(String[] args) throws Exception {
        String yml = Files.readString(Path.of("backend/src/main/resources/application.yml"), StandardCharsets.UTF_8);
        String baseUrl = resolve(first(yml, "(?m)^\\s*base-url:\\s*\"?([^\"\\r\\n]+?)\"?\\s*$"));
        String key = resolve(first(yml, "(?m)^\\s*key:\\s*\"?([^\"\\r\\n]+?)\"?\\s*$"));

        Field f = ChatClientImpl.class.getDeclaredField("CHAT_INSTRUCTIONS");
        f.setAccessible(true);
        String instructions = (String) f.get(null);
        System.out.println("使用的提示词（末 60 字）："
                + instructions.substring(Math.max(0, instructions.length() - 60)));
        System.out.println("endpoint = " + baseUrl + "   model = " + MODEL + "\n");

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .build();

        for (String prompt : PROMPTS) {
            run(client, prompt, instructions);
        }

        // 额外做一次流式测试：看搜索前的"旁白"会不会一起推给前端
        streamRun(client, "史上MC评分最高的游戏", instructions);
    }

    /** 按线上一致的方式做流式调用，打印 item 的 phase 和每个 delta */
    private static void streamRun(WebClient client, String prompt, String instructions) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("stream", true);
        body.put("input", List.of(Map.of(
                "type", "message", "role", "user",
                "content", List.of(Map.of("type", "input_text", "text", prompt)))));
        body.put("reasoning", Map.of("effort", "none"));
        body.put("max_output_tokens", 800);
        body.put("instructions", instructions);
        body.put("tools", List.of(Map.of("type", "web_search")));

        System.out.println("===== 流式测试：" + prompt + " =====");
        StringBuilder all = new StringBuilder();
        Map<String, String> phaseOfItem = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(1);
        try {
            client.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .subscribe(ev -> {
                        try {
                            JsonNode node = MAPPER.readTree(ev);
                            String type = node.path("type").asText();
                            if ("response.output_item.added".equals(type)) {
                                JsonNode item = node.path("item");
                                String id = item.path("id").asText();
                                String phase = item.path("phase").asText();
                                phaseOfItem.put(id, phase);
                                System.out.println("  [item] type=" + item.path("type").asText()
                                        + "  phase=" + phase);
                            } else if ("response.web_search_call.in_progress".equals(type)
                                    || "response.web_search_call.searching".equals(type)
                                    || "response.web_search_call.completed".equals(type)) {
                                System.out.println("  [搜索] " + type);
                            } else if ("response.output_text.delta".equals(type)) {
                                String id = node.path("item_id").asText();
                                String delta = node.path("delta").asText();
                                all.append(delta);
                                System.out.println("  [delta·" + phaseOfItem.getOrDefault(id, "?")
                                        + "] " + delta.replace("\n", "\\n"));
                            }
                        } catch (Exception ignored) {
                        }
                    },
                    err -> {
                        System.out.println("  !! 出错：" + err.getMessage());
                        latch.countDown();
                    },
                    () -> {
                        String text = all.toString();
                        System.out.println("\n  前端会拼出的完整文本：");
                        System.out.println(text);
                        System.out.printf("  [统计] ** 出现 %d 次 → %s%n%n",
                                count(text, "\\*\\*"),
                                count(text, "\\*\\*") == 0 ? "没有 Markdown ✅" : "仍含 Markdown ❌");
                        latch.countDown();
                    });
            latch.await(180, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("流式调用失败：" + e.getMessage());
        }
    }

    private static void run(WebClient client, String prompt, String instructions) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("input", List.of(Map.of(
                "type", "message", "role", "user",
                "content", List.of(Map.of("type", "input_text", "text", prompt)))));
        body.put("reasoning", Map.of("effort", "none"));
        body.put("max_output_tokens", 800);
        body.put("instructions", instructions);
        body.put("tools", List.of(Map.of("type", "web_search")));

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
            String text = extractText(root);

            int bold = count(text, "\\*\\*");
            int heading = count(text, "(?m)^\\s*#");
            int bullet = count(text, "(?m)^\\s*[-*]\\s");
            int searches = 0;
            for (JsonNode item : root.path("output")) {
                if ("web_search_call".equals(item.path("type").asText())) searches++;
            }

            System.out.println("===== 提问：" + prompt + "  （耗时 "
                    + (System.currentTimeMillis() - start) + " ms）=====");
            System.out.println(text);
            System.out.printf("%n[统计] 联网搜索 %d 次 | ** 出现 %d 次 | 行首 # %d 次 | 行首 -/* %d 次 → %s%n%n",
                    searches, bold, heading, bullet,
                    (bold + heading + bullet == 0) ? "没发现 Markdown 标记 ✅" : "仍含 Markdown ❌");
        } catch (Exception e) {
            System.out.println("调用失败：" + e.getMessage());
        }
    }

    private static String extractText(JsonNode root) {
        for (JsonNode item : root.path("output")) {
            if (!"message".equals(item.path("type").asText())) continue;
            for (JsonNode part : item.path("content")) {
                if ("output_text".equals(part.path("type").asText())) {
                    return part.path("text").asText();
                }
            }
        }
        return "(没找到 output_text)";
    }

    private static int count(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    private static String first(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    /** 解析 yml 里的 ${VAR:default} 占位符：优先环境变量，其次默认值 */
    private static String resolve(String value) {
        if (value == null) return null;
        Matcher m = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?\\}").matcher(value.trim());
        if (m.matches()) {
            String env = System.getenv(m.group(1));
            if (env != null && !env.isBlank()) return env;
            return m.group(2);
        }
        return value;
    }
}
