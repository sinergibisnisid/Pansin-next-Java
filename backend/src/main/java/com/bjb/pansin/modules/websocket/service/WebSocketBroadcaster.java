package com.bjb.pansin.modules.websocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketBroadcaster {

    public static final String TOPIC_VAULT      = "/topic/vault";
    public static final String TOPIC_DEVICE     = "/topic/device";
    public static final String TOPIC_ALARM      = "/topic/alarm";
    public static final String TOPIC_LIVESTREAM = "/topic/livestream";

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(String topic, Object payload) {
        messagingTemplate.convertAndSend(topic, payload);
    }

    public void toUser(String username, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(username, destination, payload);
    }
}
