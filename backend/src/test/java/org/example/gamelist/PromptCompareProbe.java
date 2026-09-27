package org.example.gamelist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gamelist.client.impl.DeepSeekClientImpl;
import org.example.gamelist.config.AiConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用「当前代码里的提示词」分别打 Qwen(DashScope) 和 DeepSeek 两个端点，
 * 各跑 N 次，记录搜索次数、耗时、token 与关键字段。
 * 请求体反射取自真实实现，所以提示词永远与线上一致。
 */
public class PromptCompareProbe {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com";
    private static final String DEEPSEEK_MODEL = "deepseek-v4-pro";

    public static void main(String[] args) throws Exception {
        String game = args.length > 0 ? args[0] : "GTA6";
        int rounds = args.length > 1 ? Integer.parseInt(args[1]) : 3;

        String yml = Files.readString(Path.of("backend/src/main/resources/application.yml"), StandardCharsets.UTF_8);
        String qwenBaseUrl = first(yml, "(?m)^\\s*base-url:\\s*\"?([^\"\\r\\n]+?)\"?\\s*$");
        String qwenKey = first(yml, "(?m)^\\s*key:\\s*\"?([^\"\\r\\n]+?)\"?\\s*$");
        String qwenModel = first(yml, "(?m)^\\s*model:\\s*\"?([^\"\\r\\n]+?)\"?\\s*$");

        System.out.println("游戏 = " + game + "   每个端点跑 " + rounds + " 次\n");

        runProvider("Qwen  " + qwenModel, qwenBaseUrl, qwenKey, qwenModel, game, rounds);
        runProvider("DeepSeek " + DEEPSEEK_MODEL, DEEPSEEK_BASE_URL, System.getenv("APIKEY_GAME"), DEEPSEEK_MODEL, game, rounds);
    }

    private static void runProvider(String label, String baseUrl, String key, String model,
                                    String game, int rounds) throws Exception {
        System.out.println("===== " + label + "  (" + baseUrl + ") =====");
        if (key == null || key.isBlank()) {
            System.out.println("  跳过：没有可用 key\n");
            return;
        }
        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .build();

        AiConfig cfg = new AiConfig();
        cfg.setModel(model);
        cfg.setTemperature(0.3);
        cfg.setMaxTokens(6000);

        DeepSeekClientImpl impl = new DeepSeekClientImpl(client, cfg);
        Method build = DeepSeekClientImpl.class.getDeclaredMethod("buildRequestBody", String.class);
        build.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = new HashMap<>((Map<String, Object>) build.invoke(impl, game));

        int total = 0;
        for (int i = 1; i <= rounds; i++) {
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

                int items = 0;
                for (JsonNode item : root.path("output")) {
                    if ("web_search_call".equals(item.path("type").asText())) items++;
                }
                total += items;
                JsonNode usage = root.path("usage");
                System.out.printf("  #%d  搜索=%d次  耗时=%.1fs  token: in=%d cached=%d out=%d%n",
                        i, items, (System.currentTimeMillis() - start) / 1000.0,
                        usage.path("input_tokens").asInt(),
                        usage.path("input_tokens_details").path("cached_tokens").asInt(),
                        usage.path("output_tokens").asInt());
                printFields(root);
            } catch (Exception e) {
                System.out.printf("  #%d  失败: %s%n", i, e.getMessage());
            }
        }
        System.out.printf("  → 平均搜索 %.2f 次%n%n", total / (double) rounds);
    }

    private static void printFields(JsonNode root) {
        for (JsonNode item : root.path("output")) {
            if (!"message".equals(item.path("type").asText())) continue;
            for (JsonNode part : item.path("content")) {
                if (!"output_text".equals(part.path("type").asText())) continue;
                String text = part.path("text").asText();
                try {
                    JsonNode dto = MAPPER.readTree(text);
                    System.out.printf("       name=%s | mcRating=%s | price=%s | releaseDate=%s%n",
                            dto.path("name").asText(), dto.path("mcRating").asText(),
                            dto.path("price").asText(), dto.path("releaseDate").asText());
                } catch (Exception e) {
                    System.out.println("       （正文不是合法 JSON）"
                            + text.substring(0, Math.min(80, text.length())));
                }
                return;
            }
        }
    }

    private static String first(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }
}
