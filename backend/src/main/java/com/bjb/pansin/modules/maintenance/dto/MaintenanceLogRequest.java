package com.bjb.pansin.modules.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class MaintenanceLogRequest {
    private UUID planId;
    private UUID vaultId;
    private UUID deviceId;
    @NotBlank private String type;
    private String notes;
    private String status;
}
