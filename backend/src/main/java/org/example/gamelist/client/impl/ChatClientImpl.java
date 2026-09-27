package org.example.gamelist.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.gamelist.client.ChatClient;
import org.example.gamelist.common.UserContext;
import org.example.gamelist.config.AiConfig;
import org.example.gamelist.mapper.GameMapper;
import org.example.gamelist.util.ChatHistoryStore;
import org.example.gamelist.util.SseSession;
import org.example.gamelist.vo.GameVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;

@Slf4j
@Component
public class ChatClientImpl implements ChatClient {

    /** 对话系统提示词（提成常量，便于写探针时反射读取同一份文本） */
    private static final String CHAT_INSTRUCTIONS =
            "你是一位游戏信息助手，回答用户游戏相关的问题；如果问题与游戏不相关，" +
            "则回答：不是哥们，能不能问点游戏相关的？" +
            "具体人设：你叫主机龙，你是一位喜欢单机游戏、讨厌例如原神等二次元抽卡养成游戏的人，口吻要幽默，会玩网络热梗，" +
            "例如：原神？狗都不玩。说话时习惯在吐槽、反讽、反问或自嘲的句子里带一两个「黄豆表情」" +
            "（😅😂🤣😭🥲😎🤡🙄😤😳），放在句尾；平铺直叙的说明性内容里不要用，也不要每句话都挂一个，" +
            "更不要用 ⚠️🎮 这类符号表情代替。" +
            "输出要求：只输出纯文本，禁止任何 Markdown——不要用 ** 加粗、# 标题、- 或 * 做项目符号、" +
            "反引号代码块、| 做表格；分点用「1. 2. 3.」或「第一、第二」直接写在正文里。" +
            "禁止输出过程旁白、预告和思考过程：不要出现「我来查一下」「我先查一下」「让我搜索一下」" +
            "「我刚刚搜索了」「根据搜索结果」「我帮你查了下」这类交代自己动作或资料出处的句子；" +
            "也禁止出现「我马上去查」「我这就去确认」「马上给你准信」「稍等」「让我确认最新说法」这类" +
            "预告自己下一步要干什么的句子，开头不要，结尾更不要，" +
            "更不许把一次回答拆成「先说要去查、下次再给答案」。" +
            "必须直接给结论：第一句话就是信息本身，一次回答把结论说完，" +
            "需要联网就直接调用工具，禁止用任何文字说明你要去查，也不要把思考、推理、" +
            "计划写进回答（包括英文的 Summary、thinking 或 </think> 这类标签）。" +
            "联网规则（必须遵守）：" +
            "1. 涉及随时间变化的信息——「最新、最近、刚发售、什么时候出、发售了吗、价格、评分、销量、" +
            "版本更新、DLC」、「今年、明年、这个月、本月、这周」这类相对时间，以及「某游戏呢？」这种短追问——" +
            "都必须先在这一次回答里真的调用 web_search 再作答；没有检索就不许下「已发售／未发售／尚未公布」" +
            "这类结论。举例：用户问「GTA6 发售了吗」，或者只追问一句「空之轨迹2nd呢？」，都要先搜再答。" +
            "一次调用里把要查的点一起查完（一次搜索可带多个查询词），不要反复检索。" +
            "2. 问某个时间段的发售档期时，检索词里必须带年份；用户没写年份就按当前年份理解，" +
            "不要把往年的档期当成即将到来的档期，也不要复用你之前回答里给过的名单。" +
            "3. 以搜索结果为准：查不到就如实说「我没有查到最新信息」，不要编造，也不要退回凭记忆作答；" +
            "同样不允许用「我的知识有截止时间」为理由拒绝回答。" +
            "格式要求：把回答写成一段连续的中文文本，不使用任何换行；分点内容用「1. 2. 3.」直接串在句子里。" +
            "篇幅由问题本身决定：简单问题一两句答完即可，复杂问题该展开就展开；" +
            "既不要为了省事漏掉要点，也不要为了凑长度啰嗦。";

    @Autowired
    private WebClient webClient;
    @Autowired
    private AiConfig aiConfig;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ChatHistoryStore chatHistoryStore;
    @Autowired
    private GameMapper gameMapper;

    @Override
    public void getAnswer(String msg){
        // UserContext 是 ThreadLocal：必须在当前 HTTP 请求线程里取出并捕获进 lambda，
        // 因为 subscribe 的回调跑在 Reactor Netty 线程上，那里取不到、也已经被 afterCompletion 清掉了
        Integer userId = UserContext.getCurrentId();
        if (userId == null) {
            log.warn("拿不到当前用户，无法推送 SSE");
            return;
        }
        final String clientId = String.valueOf(userId);

        // ① 用户消息先落库，② 再把完整上下文读出来交给模型
        chatHistoryStore.appendUser(clientId, msg);
        List<Map<String, Object>> input = chatHistoryStore.load(clientId);
        String profile = buildGameProfile(userId);

        Map<String, Object> requestBody = Map.of(
                "model", "deepseek-v4.1-flash",
                "stream", true,
                "input", input,
                "reasoning", Map.of("effort", "none"),
                "max_output_tokens",800,
                "instructions", buildInstructionsWithDate(profile),
                "tools", List.of(Map.of("type", "web_search"))
        );

        webClient.post()
            .uri("/responses")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToFlux(String.class)
            .takeUntil(this::isFinalEvent)      // ⭐ 先检测最终事件，终止流
            .doOnNext(event -> {saveAnswerIfCompleted(clientId, event);})  // ⭐ 从完成事件取全文落库
            .mapNotNull(this::extractTextDelta)     // ⭐ 再提取 delta（最终事件返回 null，被过滤）
            .doFinally(signal -> sendDone(clientId))    // ⭐ 无论正常结束还是出错，都补一个结束帧
            .subscribe(
                    data -> {
                        // ⭐ 转义成 JSON 字符串再发：delta 里可能含真换行，
                        // 裸换行会被 SSE 当成帧分隔符吃掉，换行后面的文字甚至会被整段丢弃
                        try {
                            SseSession.send(clientId, objectMapper.writeValueAsString(data));
                        } catch (Exception e) {
                            log.warn("SSE 载荷转义失败，跳过本次增量", e);
                        }
                    },
                    err -> log.error("对话流式调用失败 | clientId: {}", clientId, err),
                    () -> log.debug("对话流结束 | clientId: {}", clientId)
            );
    }
    private String buildInstructionsWithDate(String profile) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        String head = String.format(
                "当前日期：%d年%d月%d日（%s），北京时间（Asia/Shanghai）。" +
                        "凡是涉及「这个月、本月、这周、今天、最近、今年」这类相对时间的问题，" +
                        "一律以上述日期为基准去理解和检索，不要使用你训练数据里的时间。",
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth(),
                today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINA)
        );
        if (profile == null || profile.isEmpty()) {
            return head + CHAT_INSTRUCTIONS;
        }
        return head + profile + CHAT_INSTRUCTIONS;
    }

    /** 把用户收藏库拼成「游戏名(星级)」的字符串，直接塞进提示词；空库返回空串 */
    private String buildGameProfile(Integer userId) {
        List<GameVO> games;
        try {
            games = gameMapper.selectGameBriefByUserId(userId);
        } catch (Exception e) {
            log.warn("查询用户游戏库失败，本轮不带游戏库 | userId: {}", userId, e);
            return "";
        }
        if (games == null || games.isEmpty()) {
            return "";
        }
        StringBuilder profile = new StringBuilder("他的游戏库（括号里是他打的星级）：");
        for (GameVO game : games) {
            if (game.getName() == null || game.getName().isBlank()) {
                continue;
            }
            profile.append(game.getName());
            Integer rating = game.getRating();
            if (rating != null && rating > 0) {
                profile.append("(").append(rating).append("星)");
            }
            profile.append("、");
        }
        return profile.toString();
    }
    private void saveAnswerIfCompleted(String clientId, String sseData) {
        String full = extractFinalText(sseData);
        if (full != null && !full.isBlank()) {
            chatHistoryStore.appendAssistant(clientId, full);
        }
    }

    private void sendDone(String clientId) {
        try {
            SseSession.send(clientId, objectMapper.writeValueAsString("DONE"));
        } catch (Exception e) {
            log.warn("发送结束帧失败 | clientId: {}", clientId, e);
        }
    }

    public String extractFinalText(String sseData) {
        if (sseData == null || sseData.isEmpty()) {
            return null;
        }
        try {
            JsonNode event = objectMapper.readTree(sseData);
            if (!"response.completed".equals(event.path("type").asText())) {
                return null;
            }
            for (JsonNode item : event.path("response").path("output")) {
                if (!"message".equals(item.path("type").asText())) {
                    continue;
                }
                for (JsonNode content : item.path("content")) {
                    if ("output_text".equals(content.path("type").asText())) {
                        return content.path("text").asText();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取最终回答失败 | 事件: {}", sseData, e);
        }
        return null;
    }
    public String extractTextDelta(String sseData) {
        if (sseData == null || sseData.isEmpty()) {
            return null;
        }

        try {
            JsonNode event = objectMapper.readTree(sseData);
            String type = event.path("type").asText();

            // ⭐ 只处理文本增量事件
            if ("response.output_text.delta".equals(type)) {
                JsonNode delta = event.path("delta");
                if (delta.isMissingNode() || delta.isNull()) {
                    return null;
                }
                return delta.asText();
            }
            // 其他事件类型忽略
            return null;
        } catch (Exception e) {
            log.warn("解析 SSE 事件失败: {}", sseData);
            return null;
        }
    }
    public boolean isFinalEvent(String sseData) {
        if (sseData == null || sseData.isEmpty()) {
            return false;
        }

        try {
            JsonNode event = objectMapper.readTree(sseData);
            String type = event.path("type").asText();

            return "response.completed".equals(type)
                    || "response.incomplete".equals(type)
                    || "response.failed".equals(type);

        } catch (Exception e) {
            return false;
        }
    }
}
