package com.bjb.pansin.modules.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class MaintenancePlanRequest {
    private UUID vaultId;
    private UUID deviceId;
    @NotBlank private String type;
    @NotBlank private String name;
    private String description;
    @NotNull private Integer intervalDays;
    private Instant nextDueAt;
}
