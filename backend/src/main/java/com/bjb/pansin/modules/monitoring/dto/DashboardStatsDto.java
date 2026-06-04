package com.bjb.pansin.modules.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalBranches;
    private long activeVaults;
    private long activeAlarms;
    private long onlineDevices;
    private long totalDevices;
    private long activeUsers;
    private long todayActivities;
    private long mqttConnections;
    private String serverStatus;
}
