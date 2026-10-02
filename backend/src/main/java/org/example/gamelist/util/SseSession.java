package org.example.gamelist.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SseSession {
    private static Map<String,SseEmitter> SESSION = new ConcurrentHashMap<>();
    public static void add(String id, SseEmitter emitter){
        final SseEmitter oldEmitter = SESSION.get(id);
        if(oldEmitter!= null){
            try {
                // 安全关闭旧的emitter
                oldEmitter.complete();
            } catch (Exception e) {
                log.warn("MSG: Error completing old emitter | ID: {} | Error: {}", id, e.getMessage());
            }
        }
        SESSION.put(id, emitter);
    }
    /**
     * 条件移除：只有该 id 仍然指向同一个 emitter 时才移除并关闭。
     * 生命周期回调（onCompletion/onTimeout/onError）和发送失败必须走这个重载——
     * 否则同名重连后，旧连接迟到的回调会按 id 误删掉新会话。
     */
    public static boolean del(String id, SseEmitter emitter){
        if(emitter == null || !SESSION.remove(id, emitter)){
            return false;
        }
        try {
            emitter.complete();
        } catch (Exception e) {
            log.warn("关闭 emitter 失败 | ID: {} | Error: {}", id, e.getMessage());
        }
        return true;
    }
    public static boolean send(String id,Object msg){
        final SseEmitter emitter = SESSION.get(id);
        if (emitter != null){
            try {
                emitter.send(msg);
                return true;
            }catch (Exception e){
                // 可能是 IOException，也可能是 emitter 已 complete 抛的 IllegalStateException
                log.warn("发送失败，移除并关闭会话 | ID: {} | Error: {}", id, e.getMessage());
                del(id, emitter);
            }
        }else {log.warn("会话不存在，消息被丢弃 | ID: {}", id);}
        return false;
    }

}

