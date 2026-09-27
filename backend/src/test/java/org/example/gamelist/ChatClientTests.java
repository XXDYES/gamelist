package org.example.gamelist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gamelist.client.impl.ChatClientImpl;
import org.example.gamelist.config.AiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ChatClient 冒烟测试：验证 WebClient 能否接收大模型流式输出。
 * <p>
 * 用法：IDEA 里直接运行 getAnswer()；
 * 命令行 java -cp ... org.example.gamelist.ChatClientTests "介绍一下原神"
 */
class ChatClientTests {

    private static final String BASE_URL = "https://api.deepseek.com";
    private static final String MODEL = "deepseek-v4-flash";
    private static final String DEFAULT_PROMPT = "用三句话介绍星露谷物语";
    private static final int MAX_OUTPUT_TOKENS = 800;

    @Test
    void getAnswer() throws Exception {
        run(DEFAULT_PROMPT);
    }

    public static void main(String[] args) throws Exception {
        run(args.length > 0 ? args[0] : DEFAULT_PROMPT);
    }

    private static void run(String prompt) throws Exception {
        System.out.println("===== 提示词：" + prompt + " =====");
        WebClient client = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + System.getenv("APIKEY_GAME"))
                .build();

        rawProbe(client, prompt);
        chatClientProbe(client, prompt);
    }

    /** ① 原始流探测：打印事件时间线，并把 delta 拼成完整回答 */
    private static void rawProbe(WebClient client, String prompt) throws InterruptedException {
        System.out.println("\n----- ① 原始 SSE 事件时间线 -----");
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder answer = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(1);
        long start = System.currentTimeMillis();

        client.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(Map.of(
                        "model", MODEL,
                        "input", prompt,
                        "stream", true,
                        "reasoning", Map.of("effort", "none"),
                        "max_output_tokens", MAX_OUTPUT_TOKENS))
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(
                        data -> {
                            long cost = System.currentTimeMillis() - start;
                            try {
                                JsonNode json = mapper.readTree(data);
                                String type = json.path("type").asText();
                                if ("response.output_text.delta".equals(type)) {
                                    String delta = json.path("delta").asText();
                                    answer.append(delta);
                                    System.out.printf("[%5d ms] delta  %s%n", cost, delta.replace("\n", "\\n"));
                                } else if ("response.completed".equals(type)) {
                                    JsonNode usage = json.path("response").path("usage");
                                    System.out.printf("[%5d ms] response.completed   usage: input=%d output=%d total=%d%n",
                                            cost,
                                            usage.path("input_tokens").asInt(),
                                            usage.path("output_tokens").asInt(),
                                            usage.path("total_tokens").asInt());
                                } else {
                                    System.out.printf("[%5d ms] %s%n", cost, type);
                                }
                            } catch (Exception e) {
                                System.out.printf("[%5d ms] (解析失败) %s%n", cost, data);
                            }
                        },
                        err -> {
                            System.out.println("!! onError: " + err);
                            latch.countDown();
                        },
                        () -> {
                            System.out.println("\n----- 拼接后的完整回答 -----");
                            System.out.println(answer);
                            System.out.println("----- 总耗时 " + (System.currentTimeMillis() - start) + " ms -----");
                            latch.countDown();
                        });

        if (!latch.await(90, TimeUnit.SECONDS)) {
            System.out.println("!! 90 秒内没有收到完成信号");
        }
    }

    /** ② 调用被测的 ChatClientImpl（void + subscribe 打印原始 JSON） */
    private static void chatClientProbe(WebClient client, String prompt) throws InterruptedException {
        System.out.println("\n----- ② ChatClientImpl.getAnswer() -----");

        AiConfig config = new AiConfig();
        config.setModel(MODEL);

        ChatClientImpl chatClient = new ChatClientImpl();
        // 字段是 @Autowired 私有的、没有构造器也没有 setter，只能反射塞进去
        ReflectionTestUtils.setField(chatClient, "webClient", client);
        ReflectionTestUtils.setField(chatClient, "aiConfig", config);

        long start = System.currentTimeMillis();
        chatClient.getAnswer(prompt);
        System.out.println("getAnswer 已返回，耗时 " + (System.currentTimeMillis() - start)
                + " ms（订阅是异步的，输出还在后面）");

        // 该方法没有返回值也没有回调，只能盲等
        Thread.sleep(25000);
        System.out.println("----- 等待结束，共 " + (System.currentTimeMillis() - start) + " ms -----");
    }
}
