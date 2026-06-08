package com.bjb.pansin.events;

import com.bjb.pansin.modules.audit.service.AuditService;
import com.bjb.pansin.modules.vault.event.AlarmTriggeredEvent;
import com.bjb.pansin.modules.vault.event.VaultClosedEvent;
import com.bjb.pansin.modules.vault.event.VaultOpenedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka listeners that consume events from the same topics events are produced to.
 * They handle cross-cutting concerns asynchronously - audit log persistence and similar fan-out work.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
public class KafkaEventConsumers {

    private final AuditService auditService;

    @KafkaListener(topics = "vault-events", groupId = "pansin-audit", containerFactory = "kafkaListenerContainerFactory")
    public void onVaultOpened(VaultOpenedEvent event) {
        log.debug("[kafka:vault-events:open] {}", event);
        auditService.log("VAULT_OPEN", "vault", event.getVaultId(),
                "Vault opened via " + event.getOpenMethod(),
                null, Map.of("sessionId", event.getSessionId(), "userId", String.valueOf(event.getUserId())),
                null, null);
    }

    @KafkaListener(topics = "vault-events", groupId = "pansin-audit-close",
            containerFactory = "kafkaListenerContainerFactory")
    public void onVaultClosed(VaultClosedEvent event) {
        log.debug("[kafka:vault-events:close] {}", event);
        auditService.log("VAULT_CLOSE", "vault", event.getVaultId(),
                "Vault closed (duration=%ds, exceeded=%s)".formatted(
                        event.getDurationSeconds(), event.isExceededLimit()),
                null,
                Map.of("sessionId", event.getSessionId(),
                        "duration", event.getDurationSeconds(),
                        "exceeded", event.isExceededLimit()),
                null, null);
    }

    @KafkaListener(topics = "alarm-events", groupId = "pansin-alarm-audit",
            containerFactory = "kafkaListenerContainerFactory")
    public void onAlarm(AlarmTriggeredEvent event) {
        log.debug("[kafka:alarm-events] {}", event);
        auditService.log("ALARM_RAISED", "alarm", event.getVaultId(),
                "Alarm: " + event.getType() + " - " + event.getMessage(),
                null,
                Map.of("type", event.getType().name(),
                        "severity", String.valueOf(event.getSeverity())),
                null, null);
    }
}
