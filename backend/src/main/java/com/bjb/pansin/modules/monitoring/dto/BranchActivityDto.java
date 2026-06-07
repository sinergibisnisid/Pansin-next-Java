package com.bjb.pansin.modules.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchActivityDto {
    private UUID branchId;
    private String branchCode;
    private String branchName;
    private long accessCount;
    private long alarmCount;
}
