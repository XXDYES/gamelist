package org.example.gamelist.Controller;

import org.example.gamelist.client.ChatClient;
import org.example.gamelist.common.Result;
import org.example.gamelist.common.UserContext;
import org.example.gamelist.exception.BusinessException;
import org.example.gamelist.service.SseService;
import org.example.gamelist.util.ChatHistoryStore;
import org.example.gamelist.vo.HistoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private SseService sseService;
    @Autowired
    private ChatHistoryStore chatHistoryStore;
    @GetMapping("/chatmsg")
    public Result<Void> chatMsg(@RequestParam("msg")String msg){
        if (msg == null || msg.trim().isEmpty()) {
            throw new BusinessException("消息不能为空");
        }
        chatClient.getAnswer(msg);
        return Result.success();
    }
    @GetMapping("/chatconnect")
    public SseEmitter chatConnect(@RequestParam("clientId")String id){
        return sseService.start(id);
    }
    @GetMapping("/gethistory")
    public Result<List<HistoryVO>> getHistory(){
        Integer userId = UserContext.getCurrentId();
        if (userId == null) {
            return Result.success(new ArrayList<>());
        }
        List<Map<String, Object>> items = chatHistoryStore.load(String.valueOf(userId));
        List<HistoryVO> history = new ArrayList<>(items.size());
        for (Map<String, Object> item : items) {
            HistoryVO vo = chatHistoryStore.toHistoryVO(item);
            if (vo != null) {
                history.add(vo);
            }
        }
        return Result.success(history);
    }

}
