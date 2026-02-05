package com.sysbreak.ems.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 遥测数据 WebSocket 推送
 *
 * @author sysbreak
 * @since 2026-01-16
 */
@Slf4j
@Component
public class TelemetryWebSocketHandler implements WebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket 连接建立: {}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        // EMS 场景：前端一般只接收，不主动发
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket 连接关闭: {}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 异常", exception);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 广播遥测数据
     */
    public void broadcast(String message) {
        sessions.forEach(session -> {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("WebSocket 推送失败", e);
            }
        });
    }

}
