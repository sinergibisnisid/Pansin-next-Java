package com.bjb.pansin.modules.mqtt.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
public class HeartbeatPayload {
    private UUID deviceId;
    private BigDecimal cpuLoad;
    private BigDecimal memoryLoad;
    private Integer signalQuality;
    private Long uptimeSeconds;
    private Map<String, Object> metadata;
}
