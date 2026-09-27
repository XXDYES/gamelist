package org.example.gamelist.service;

import lombok.extern.slf4j.Slf4j;
import org.example.gamelist.util.SseSession;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SseService {
    private final ScheduledExecutorService heartbeatScheduler =
            Executors.newSingleThreadScheduledExecutor();

    public SseEmitter start(String clientId){
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        SseSession.add(clientId,emitter);
        try {
            // 发送连接成功消息
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("连接已建立")
                    .id(String.valueOf(System.currentTimeMillis())));
        } catch (IOException e) {
            log.error("emitter连接失败");
        }
        ScheduledFuture<?> heartbeatTask = heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                // 发送 SSE 注释帧，前端不会触发 onmessage
                emitter.send(SseEmitter.event().comment("keepalive"));
            } catch (Exception e) {
                log.warn("心跳发送失败，连接可能已断开 | clientId: {}", clientId);
                // 不抛异常，任务自然停止
            }
        }, 0, 30, TimeUnit.SECONDS);
        emitter.onCompletion(() -> {heartbeatTask.cancel(true);SseSession.del(clientId, emitter);});
        emitter.onTimeout(() -> {heartbeatTask.cancel(true);SseSession.del(clientId, emitter);});
        emitter.onError(e -> {heartbeatTask.cancel(true);SseSession.del(clientId, emitter);});
        return emitter;
    }
}
