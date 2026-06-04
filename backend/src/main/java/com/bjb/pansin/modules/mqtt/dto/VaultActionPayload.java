package com.bjb.pansin.modules.mqtt.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class VaultActionPayload {
    private UUID vaultId;
    private UUID userId;
    private String method;
    private String reason;
}
