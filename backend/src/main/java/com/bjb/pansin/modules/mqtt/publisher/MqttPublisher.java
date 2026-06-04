package com.bjb.pansin.modules.mqtt.publisher;

import com.bjb.pansin.modules.mqtt.config.MqttConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.integration.mqtt.support.MqttHeaders.TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttPublisher {

    private final MessageChannel mqttOutboundChannel;
    private final ObjectMapper objectMapper;

    public void publish(String topic, Object payload) {
        try {
            String body = payload instanceof String s ? s : objectMapper.writeValueAsString(payload);
            mqttOutboundChannel.send(MessageBuilder
                    .withPayload(body)
                    .setHeader(TOPIC, topic)
                    .build());
            log.debug("[MQTT] -> {} : {}", topic, body);
        } catch (JsonProcessingException ex) {
            log.error("[MQTT] failed to serialize payload for topic {}", topic, ex);
        } catch (Exception ex) {
            log.error("[MQTT] publish failed on topic {}", topic, ex);
        }
    }
}
