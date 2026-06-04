package com.bjb.pansin.modules.mqtt.consumer;

import com.bjb.pansin.modules.fingerprint.service.FingerprintService;
import com.bjb.pansin.modules.heartbeat.service.HeartbeatService;
import com.bjb.pansin.modules.mqtt.config.MqttConfig;
import com.bjb.pansin.modules.mqtt.config.MqttProperties;
import com.bjb.pansin.modules.mqtt.dto.FingerprintScanPayload;
import com.bjb.pansin.modules.mqtt.dto.HeartbeatPayload;
import com.bjb.pansin.modules.mqtt.dto.VaultActionPayload;
import com.bjb.pansin.modules.vault.service.VaultSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static org.springframework.integration.mqtt.support.MqttHeaders.RECEIVED_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttInboundConsumer {

    private final ObjectMapper objectMapper;
    private final MqttProperties props;
    private final FingerprintService fingerprintService;
    private final HeartbeatService heartbeatService;
    private final VaultSessionService vaultSessionService;

    @ServiceActivator(inputChannel = MqttConfig.INBOUND_CHANNEL)
    public void onMessage(Message<String> message) {
        String topic = (String) message.getHeaders().get(RECEIVED_TOPIC);
        String payload = message.getPayload();
        log.debug("[MQTT] <- {} : {}", topic, payload);

        try {
            if (topic == null) return;

            if (topic.equals(props.getTopics().getFingerprintScan())) {
                FingerprintScanPayload p = objectMapper.readValue(payload, FingerprintScanPayload.class);
                fingerprintService.handleScan(p.getDeviceId(), p.getUserId(),
                        p.getTemplateId(), p.getQuality(), p.getRaw());

            } else if (topic.equals(props.getTopics().getDeviceHeartbeat())) {
                HeartbeatPayload p = objectMapper.readValue(payload, HeartbeatPayload.class);
                heartbeatService.recordHeartbeat(p.getDeviceId(), p.getCpuLoad(),
                        p.getMemoryLoad(), p.getSignalQuality(),
                        p.getUptimeSeconds(), p.getMetadata());

            } else if (topic.equals(props.getTopics().getVaultOpen())) {
                VaultActionPayload p = objectMapper.readValue(payload, VaultActionPayload.class);
                vaultSessionService.openVault(p.getVaultId(), p.getUserId(),
                        p.getMethod() != null ? p.getMethod() : "MQTT", null);

            } else if (topic.equals(props.getTopics().getVaultClose())) {
                VaultActionPayload p = objectMapper.readValue(payload, VaultActionPayload.class);
                vaultSessionService.closeVault(p.getVaultId(), p.getUserId(),
                        p.getMethod() != null ? p.getMethod() : "MQTT", null);

            } else {
                log.debug("[MQTT] unhandled topic {}", topic);
            }
        } catch (Exception ex) {
            log.error("[MQTT] error processing topic {}", topic, ex);
        }
    }
}
