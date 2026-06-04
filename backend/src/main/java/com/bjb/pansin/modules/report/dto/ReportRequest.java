package com.bjb.pansin.modules.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ReportRequest {
    @NotBlank
    private String name;
    @NotNull
    private ReportType type;
    @NotNull
    private ReportFormat format;
    private Instant from;
    private Instant to;
    private UUID branchId;
    private UUID vaultId;

    public enum ReportType { ACCESS_LOG, ALARM_LOG, FINGERPRINT_LOG, MAINTENANCE_LOG }
    public enum ReportFormat { PDF, EXCEL, CSV }
}
