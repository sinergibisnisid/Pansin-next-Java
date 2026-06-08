package com.bjb.pansin.events;

import com.bjb.pansin.common.enums.AlarmType;
import com.bjb.pansin.modules.mqtt.config.MqttProperties;
import com.bjb.pansin.modules.mqtt.publisher.MqttPublisher;
import com.bjb.pansin.modules.vault.event.AlarmTriggeredEvent;
import com.bjb.pansin.modules.vault.event.VaultClosedEvent;
import com.bjb.pansin.modules.vault.event.VaultOpenedEvent;
import com.bjb.pansin.modules.websocket.service.WebSocketBroadcaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class VaultEventBridge {

    private final MqttPublisher mqttPublisher;
    private final MqttProperties mqttProperties;
    private final WebSocketBroadcaster broadcaster;
    private final ApplicationEventPublisher applicationEventPublisher;
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.enabled:false}")
    private boolean kafkaEnabled;

    public VaultEventBridge(MqttPublisher mqttPublisher, MqttProperties mqttProperties,
                           WebSocketBroadcaster broadcaster, ApplicationEventPublisher applicationEventPublisher) {
        this.mqttPublisher = mqttPublisher;
        this.mqttProperties = mqttProperties;
        this.broadcaster = broadcaster;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private static final String TOPIC_VAULT = "vault-events";
    private static final String TOPIC_ALARM = "alarm-events";

    @Async("taskExecutor")
    @EventListener
    public void onOpened(VaultOpenedEvent event) {
        broadcaster.broadcast(WebSocketBroadcaster.TOPIC_VAULT, event);
        if (kafkaEnabled && kafkaTemplate != null) {
            kafkaTemplate.send(TOPIC_VAULT, "open:" + event.getVaultId(), event);
        }
        mqttPublisher.publish(mqttProperties.getTopics().getVaultOpen(), event);
    }

    @Async("taskExecutor")
    @EventListener
    public void onClosed(VaultClosedEvent event) {
        broadcaster.broadcast(WebSocketBroadcaster.TOPIC_VAULT, event);
        if (kafkaEnabled && kafkaTemplate != null) {
            kafkaTemplate.send(TOPIC_VAULT, "close:" + event.getVaultId(), event);
        }
        mqttPublisher.publish(mqttProperties.getTopics().getVaultClose(), event);

        if (event.isExceededLimit()) {
            applicationEventPublisher.publishEvent(new AlarmTriggeredEvent(
                    event.getVaultId(), null, event.getSessionId(),
                    AlarmType.SESSION_TIMEOUT, "HIGH",
                    "Vault session exceeded max duration: %d seconds".formatted(event.getDurationSeconds()),
                    Instant.now()));
        }
    }

    @Async("taskExecutor")
    @EventListener
    public void onAlarm(AlarmTriggeredEvent event) {
        if (kafkaEnabled && kafkaTemplate != null) {
            kafkaTemplate.send(TOPIC_ALARM, "alarm:" + event.getVaultId(), event);
        }
        mqttPublisher.publish(mqttProperties.getTopics().getVaultAlarm(), event);
    }
}
