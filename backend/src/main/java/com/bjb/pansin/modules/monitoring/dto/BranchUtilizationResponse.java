package com.bjb.pansin.modules.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchUtilizationResponse {
    private List<BranchUtilizationItem> branches;
    private List<BranchUtilizationTrend> weeklyTrend;
    private List<BranchUtilizationStatus> statusDistribution;
    private BranchUtilizationSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchUtilizationItem {
        private UUID branchId;
        private String branchCode;
        private String branchName;
        private long accessCount;
        private long alarmCount;
        private double averageDurationMinutes;
        private long activeVaultCount;
        private long totalVaultCount;
        private Instant lastActivityAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchUtilizationTrend {
        private LocalDate date;
        private long accessCount;
        private long alarmCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchUtilizationStatus {
        private String status;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchUtilizationSummary {
        private long totalAccess;
        private double averageDurationMinutes;
        private long totalAlarms;
        private long activeBranches;
        private long totalBranches;
    }
}
