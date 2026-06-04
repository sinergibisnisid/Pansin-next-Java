package com.bjb.pansin.modules.mqtt.dto;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class FingerprintScanPayload {
    private UUID deviceId;
    private UUID userId;
    private String templateId;
    private Integer quality;
    private Map<String, Object> raw;
}
