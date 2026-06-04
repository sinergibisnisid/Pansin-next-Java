package com.bjb.pansin.modules.websocket.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WebSocketSessionRegistry {

    private final AtomicInteger active = new AtomicInteger(0);

    public int activeSessions() {
        return active.get();
    }

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        active.incrementAndGet();
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {
        active.updateAndGet(c -> Math.max(0, c - 1));
    }
}
