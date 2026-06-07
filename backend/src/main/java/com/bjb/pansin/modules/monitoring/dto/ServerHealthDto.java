package com.bjb.pansin.modules.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerHealthDto {
    private String status;
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private BigDecimal diskUsage;
    private Boolean mqttConnected;
    private Integer websocketSessions;
    private Instant lastCheckedAt;
    private List<ServiceStatusDto> services;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceStatusDto {
        private String name;
        private String status;
        private String description;
    }
}
