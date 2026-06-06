package com.bjb.pansin.modules.mqtt.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.modules.mqtt.config.MqttProperties;
import com.bjb.pansin.modules.mqtt.dto.MqttStatusDto;
import com.bjb.pansin.modules.mqtt.service.MqttBrokerStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "MQTT", description = "MQTT broker status and configuration")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/mqtt")
@RequiredArgsConstructor
public class MqttController {

    private final MqttProperties props;
    private final MqttBrokerStatus brokerStatus;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<MqttStatusDto>> getStatus() {
        log.info("GET /mqtt/status called");

        MqttProperties.Topics t = props.getTopics();

        List<MqttStatusDto.TopicInfo> topics = List.of(
            MqttStatusDto.TopicInfo.builder().topic(t.getVaultOpen()).description("Vault open events").qos(props.getQos()).build(),
            MqttStatusDto.TopicInfo.builder().topic(t.getVaultClose()).description("Vault close events").qos(props.getQos()).build(),
            MqttStatusDto.TopicInfo.builder().topic(t.getVaultAlarm()).description("Vault alarm events").qos(props.getQos()).build(),
            MqttStatusDto.TopicInfo.builder().topic(t.getVaultEmergency()).description("Vault emergency events").qos(props.getQos()).build(),
            MqttStatusDto.TopicInfo.builder().topic(t.getFingerprintScan()).description("Fingerprint scan events").qos(props.getQos()).build(),
            MqttStatusDto.TopicInfo.builder().topic(t.getFingerprintRegister()).description("Fingerprint register events").qos(props.getQos()).build(),
            MqttStatusDto.TopicInfo.builder().topic(t.getDeviceHeartbeat()).description("Device heartbeat").qos(props.getQos()).build(),
            MqttStatusDto.TopicInfo.builder().topic(t.getDeviceStatus()).description("Device status updates").qos(props.getQos()).build()
        );

        MqttStatusDto status = MqttStatusDto.builder()
            .connected(brokerStatus.isConnected())
            .brokerUrl(props.getBrokerUrl())
            .clientId(props.getClientId())
            .username(props.getUsername())
            .useTls(props.getBrokerUrl() != null && props.getBrokerUrl().startsWith("ssl://"))
            .qos(props.getQos())
            .keepAlive(props.getKeepAlive())
            .topics(topics)
            .build();

        return ResponseEntity.ok(ApiResponse.ok("MQTT status retrieved", status));
    }
}
