package com.bjb.pansin.modules.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaultMonitorDto {
    private String id;
    private String branchId;
    private String branchName;
    private String branchCode;
    private String vaultName;
    private String status;
    private String deviceStatus;
    private String alarmStatus;
    private String currentUser;
    private Instant entryTime;
    private Long duration;
    private Double temperature;
    private Double humidity;
    private Instant lastActivity;
    private String streamUrl;
    private String snapshotUrl;
}
