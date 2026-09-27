package org.example.gamelist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gamelist.client.impl.DeepSeekClientImpl;
import org.example.gamelist.config.AiConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 用 DeepSeek 官方端点跑「收紧后的新提示词」，记录搜索次数与耗时。
 * 请求体直接反射取自真实实现，与线上完全一致（含 max_tool_calls=2）。
 */
public class DeepSeekPromptProbe {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String BASE_URL = "https://api.deepseek.com";
    private static final String MODEL = "deepseek-v4-pro";
    public static void main(String[] args) throws Exception {
        String game = args.length > 0 ? args[0] : "剑星";
        int rounds = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        String key = System.getenv("APIKEY_GAME");
        System.out.println("endpoint = " + BASE_URL + "   model = " + MODEL + "   key = "
                + (key == null ? "null" : key.substring(0, 6) + "***") + "   游戏 = " + game);

        WebClient client = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .build();

        AiConfig cfg = new AiConfig();
        cfg.setModel(MODEL);
        cfg.setTemperature(0.3);
        cfg.setMaxTokens(6000);

        DeepSeekClientImpl impl = new DeepSeekClientImpl(client, cfg);
        Method build = DeepSeekClientImpl.class.getDeclaredMethod("buildRequestBody", String.class);
        build.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = new HashMap<>((Map<String, Object>) build.invoke(impl, game));

        System.out.println("请求体字段：" + body.keySet() + "   max_tool_calls=" + body.get("max_tool_calls"));
        System.out.println();

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
                JsonNode countNode = root.path("usage").path("x_tools").path("web_search").path("count");
                String official = (countNode.isMissingNode() || countNode.isNull()) ? "-" : countNode.asText();
                total += items;

                JsonNode usage = root.path("usage");
                System.out.printf("#%d  搜索次数=%d（官方计数=%s）  耗时=%.1fs  token: in=%d cached=%d out=%d%n",
                        i, items, official, (System.currentTimeMillis() - start) / 1000.0,
                        usage.path("input_tokens").asInt(),
                        usage.path("input_tokens_details").path("cached_tokens").asInt(),
                        usage.path("output_tokens").asInt());
                printFields(root);
            } catch (Exception e) {
                System.out.printf("#%d  失败: %s%n", i, e.getMessage());
            }
        }
        System.out.printf("%n平均搜索次数 = %.2f 次%n%n", total / (double) rounds);
    }

    /** 按和线上一致的方式取正文，打印三个关键字段，看数据质量 */
    private static void printFields(JsonNode root) {
        for (JsonNode item : root.path("output")) {
            if (!"message".equals(item.path("type").asText())) continue;
            for (JsonNode part : item.path("content")) {
                if (!"output_text".equals(part.path("type").asText())) continue;
                String text = part.path("text").asText();
                try {
                    JsonNode dto = MAPPER.readTree(text);
                    System.out.printf("      name=%s | mcRating=%s | price=%s | releaseDate=%s%n",
                            dto.path("name").asText(), dto.path("mcRating").asText(),
                            dto.path("price").asText(), dto.path("releaseDate").asText());
                } catch (Exception e) {
                    System.out.println("      （正文不是合法 JSON）" + text.substring(0, Math.min(80, text.length())));
                }
                return;
            }
        }
    }
}
