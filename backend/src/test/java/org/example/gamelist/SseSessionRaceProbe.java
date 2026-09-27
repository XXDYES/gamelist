package org.example.gamelist;

import org.example.gamelist.util.SseSession;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 验证 SseSession.del(id, emitter) 的条件移除语义：
 * 同名重连后，旧连接迟到的生命周期回调不能误删新会话。
 */
public class SseSessionRaceProbe {

    public static void main(String[] args) {
        String id = "client-1";
        SseEmitter oldEmitter = new SseEmitter(30 * 60 * 1000L);
        SseEmitter newEmitter = new SseEmitter(30 * 60 * 1000L);

        SseSession.add(id, oldEmitter);          // 第一次连接
        System.out.println("add(旧连接) 完成");

        SseSession.add(id, newEmitter);          // 同名重连：旧连接被 complete，新连接上位
        System.out.println("add(新连接) 完成（旧连接已被 complete）");

        boolean byOld = SseSession.del(id, oldEmitter);
        System.out.println("旧连接迟到的回调尝试移除  -> " + byOld + "   期望 false");

        boolean sent = SseSession.send(id, "hello");
        System.out.println("往该会话发消息（验证新连接还在）-> " + sent + "   期望 true");

        boolean byNew = SseSession.del(id, newEmitter);
        System.out.println("新连接自己的回调移除      -> " + byNew + "   期望 true");

        boolean afterRemoved = SseSession.send(id, "again");
        System.out.println("会话已移除后再发消息      -> " + afterRemoved + "   期望 false");

        boolean pass = !byOld && sent && byNew && !afterRemoved;
        System.out.println(pass ? "\n结论：条件移除生效，竞态已消除" : "\n结论：行为不符合预期");
    }
}
