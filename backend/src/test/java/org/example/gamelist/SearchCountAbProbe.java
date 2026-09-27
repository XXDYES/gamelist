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
 * A/B 对比提示词对 web_search 次数的影响。
 * A = 现有提示词（反射调用真实实现取请求体）；B = 收紧后的提示词。
 * 除 instructions 与 input 外，其余参数完全一致。
 */
public class SearchCountAbProbe {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String GAME = "剑星";
    private static final int ROUNDS = 3;

    private static final String TIGHT_INSTRUCTIONS =
            "你是游戏信息助手。只输出 JSON，不要任何解释或代码块。" +
            "JSON 字符串内部禁止使用英文双引号，需要引用名称时用中文引号「」或书名号《》。" +
            "先凭知识作答：name、company、platform、type、info 直接写，" +
            "mcRating、price、releaseDate 也先按你已有的知识填写。" +
            "只有当你对 mcRating、price、releaseDate 这三个字段全部没有把握时，才允许联网搜索。" +
            "搜索后仍查不到的字段才填\"暂无\"，绝不编造，也不要为了查得更准而反复搜索。" +
            "若输入明显与电子游戏无关（食物、日常物品、人名、地名、句子、随机字符等），" +
            "不要联网搜索，直接返回无关联格式：name 原样返回输入，" +
            "除此之外的所有字段全部返回空字符串（cover 返回 null）。" +
            "注意：仅仅是你没听过、或查不到资料的游戏，仍然属于游戏，必须走正常格式，不得使用该格式。";

    public static void main(String[] args) throws Exception {
        String yml = Files.readString(Path.of("backend/src/main/resources/application.yml"), StandardCharsets.UTF_8);
        String baseUrl = first(yml, "(?m)^\\s*base-url:\\s*\"?([^\"\\r\\n]+?)\"?\\s*$");
        String key = first(yml, "(?m)^\\s*key:\\s*\"?([^\"\\r\\n]+?)\"?\\s*$");
        String model = first(yml, "(?m)^\\s*model:\\s*\"?([^\"\\r\\n]+?)\"?\\s*$");
        System.out.println("base-url = " + baseUrl + "   model = " + model + "   游戏 = " + GAME);

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
        Map<String, Object> realBody = (Map<String, Object>) build.invoke(impl, GAME);
        Map<String, Object> bodyA = new HashMap<>(realBody);

        Map<String, Object> bodyB = new HashMap<>(realBody);
        bodyB.put("instructions", TIGHT_INSTRUCTIONS);
        bodyB.put("input", tightPrompt(GAME));

        System.out.println("请求体字段（两边一致）：" + bodyA.keySet());
        System.out.println();
        for (int i = 1; i <= ROUNDS; i++) {
            run(client, "A 现有", i, bodyA);
        }
        System.out.println();
        for (int i = 1; i <= ROUNDS; i++) {
            run(client, "B 收紧", i, bodyB);
        }
    }

    private static void run(WebClient client, String label, int round, Map<String, Object> body) {
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

            JsonNode countNode = root.path("usage").path("x_tools").path("web_search").path("count");
            String official = (countNode.isMissingNode() || countNode.isNull()) ? "无此字段" : countNode.asText();

            int items = 0;
            for (JsonNode item : root.path("output")) {
                if ("web_search_call".equals(item.path("type").asText())) items++;
            }

            long cost = System.currentTimeMillis() - start;
            System.out.printf("%s #%d  ->  官方计数=%-8s  output里的web_search_call=%d  耗时=%.1fs%n",
                    label, round, official, items, cost / 1000.0);
        } catch (Exception e) {
            System.out.printf("%s #%d  ->  失败: %s%n", label, round, e.getMessage());
        }
    }

    private static String tightPrompt(String gameName) {
        return String.format(
                "查询游戏《%s》，只返回下面这个 JSON（字段名不要改）：\n" +
                        "{\"name\":\"游戏官方中文名；无法确认时原样返回输入的名称，不要中英文混搭\"," +
                        "\"company\":\"开发商，只返回一个，优先中文名称，不要中英文混搭\"," +
                        "\"platform\":\"该游戏实际登陆的平台，从 PC/PS/NS/XBOX/移动端 中选，用/分隔；只有全部登陆才写 全平台\"," +
                        "\"type\":\"游戏类型，如 开放世界、JRPG、AVG\"," +
                        "\"cover\":null," +
                        "\"info\":\"简介，100字左右，别超过150字，以游戏内容介绍为主\"," +
                        "\"price\":\"如 免费、¥298、HK$368；要求整数，不确定填 暂无，多平台优先PC端\"," +
                        "\"mcRating\":\"Metacritic 媒体均分（Metascore）纯数字，如 90。取 PC 版分数即可\"," +
                        "\"releaseDate\":\"发售日期，格式 YYYY-MM-DD；未发售或不确定填 暂无\"}\n" +
                        "输入无关时的返回格式（只在输入明显不是游戏时使用）：\n" +
                        "{\"name\":\"<原样返回输入>\",\"company\":\"\",\"platform\":\"\",\"type\":\"\",\"cover\":null," +
                        "\"info\":\"\",\"price\":\"\",\"mcRating\":\"\",\"releaseDate\":\"\"}\n" +
                        "判定示例：\n" +
                        "\"红烧肉的做法\" → 用无关格式；\"asdfghjkl\" → 用无关格式；\"今天天气怎么样\" → 用无关格式；\n" +
                        "\"剑星\" → 正常格式；\"星海旅人\"（你不认识的游戏）→ 正常格式，字段填暂无\n" +
                        "联网规则：\n" +
                        "0. 先判断输入是否与游戏相关：无关的输入不要联网，直接返回上面的无关格式。\n" +
                        "1. name/company/platform/type/info 凭知识直接写，不要为了它们搜索。\n" +
                        "2. mcRating、price、releaseDate 先按你已有的知识填写。\n" +
                        "3. 只有当你对这三个字段全部没把握时，才允许联网。此时你只有一次机会：" +
                        "必须在这一次调用里把所有要查的字段一次性查完。\n" +
                        "4. 一次搜索返回后，无论结果如何，立即停止检索并输出 JSON。" +
                        "仍不确定的字段填 暂无，禁止再次搜索，禁止打开网页。",
                gameName);
    }

    private static String first(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }
}
