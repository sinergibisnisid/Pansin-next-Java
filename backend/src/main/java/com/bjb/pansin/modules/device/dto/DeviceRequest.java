package com.bjb.pansin.modules.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DeviceRequest {
    @NotNull
    private UUID branchId;
    private UUID vaultId;
    @NotBlank
    private String deviceCode;
    @NotBlank
    private String name;
    @NotBlank
    private String type;
    private String ipAddress;
    private String macAddress;
    private String firmwareVersion;
}
