package com.bjb.pansin.modules.monitoring.service;

import com.bjb.pansin.common.enums.DeviceStatus;
import com.bjb.pansin.common.enums.VaultStatus;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.modules.activity.entity.ActivityLog;
import com.bjb.pansin.modules.activity.repository.ActivityLogRepository;
import com.bjb.pansin.modules.alarm.entity.AlarmLog;
import com.bjb.pansin.modules.alarm.repository.AlarmLogRepository;
import com.bjb.pansin.modules.branch.entity.Branch;
import com.bjb.pansin.modules.branch.repository.BranchRepository;
import com.bjb.pansin.modules.device.repository.DeviceRepository;
import com.bjb.pansin.modules.monitoring.dto.BranchActivityDto;
import com.bjb.pansin.modules.monitoring.dto.BranchUtilizationResponse;
import com.bjb.pansin.modules.monitoring.dto.DashboardStatsDto;
import com.bjb.pansin.modules.monitoring.dto.RealtimeStatsDto;
import com.bjb.pansin.modules.monitoring.dto.ServerHealthDto;
import com.bjb.pansin.modules.monitoring.dto.ServerMetricDto;
import com.bjb.pansin.modules.monitoring.dto.VaultMonitorDto;
import com.bjb.pansin.modules.monitoring.dto.VaultStatusSummaryDto;
import com.bjb.pansin.modules.monitoring.entity.ServerMonitoring;
import com.bjb.pansin.modules.monitoring.repository.ServerMonitoringRepository;
import com.bjb.pansin.modules.user.repository.UserRepository;
import com.bjb.pansin.modules.vault.entity.Vault;
import com.bjb.pansin.modules.vault.entity.VaultAccessLog;
import com.bjb.pansin.modules.vault.entity.VaultSession;
import com.bjb.pansin.modules.vault.repository.VaultAccessLogRepository;
import com.bjb.pansin.modules.vault.repository.VaultRepository;
import com.bjb.pansin.modules.vault.repository.VaultSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringService {

    private final BranchRepository branchRepository;
    private final VaultRepository vaultRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final AlarmLogRepository alarmLogRepository;
    private final VaultAccessLogRepository vaultAccessLogRepository;
    private final VaultSessionRepository vaultSessionRepository;
    private final ServerMonitoringRepository serverMonitoringRepository;

    public DashboardStatsDto getDashboardStats() {
        log.debug("Getting dashboard stats");
        try {
            long totalBranches = branchRepository.count();
            long activeVaults = vaultRepository.count();
            long totalDevices = deviceRepository.count();
            long activeUsers = userRepository.count();
            
            // Count online devices
            long onlineDevices = 0;
            try {
                onlineDevices = deviceRepository.findAll().stream()
                        .filter(d -> d.getStatus() != null && d.getStatus() == DeviceStatus.ONLINE)
                        .count();
            } catch (Exception e) {
                log.warn("Could not count online devices: {}", e.getMessage());
            }
            
            // Today activities
            long todayActivities = 0;
            try {
                Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
                todayActivities = activityLogRepository.findAll().stream()
                        .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(startOfDay))
                        .count();
            } catch (Exception e) {
                log.warn("Could not count today activities: {}", e.getMessage());
            }

            // Active alarms
            long activeAlarms = 0;
            try {
                activeAlarms = alarmLogRepository.count();
            } catch (Exception e) {
                log.warn("Could not count alarms: {}", e.getMessage());
            }
            
            return DashboardStatsDto.builder()
                    .totalBranches(totalBranches)
                    .activeVaults(activeVaults)
                    .activeAlarms(activeAlarms)
                    .onlineDevices(onlineDevices)
                    .totalDevices(totalDevices)
                    .activeUsers(activeUsers)
                    .todayActivities(todayActivities)
                    .mqttConnections(totalDevices)
                    .serverStatus("healthy")
                    .build();
        } catch (Exception e) {
            log.error("Error getting dashboard stats", e);
            return DashboardStatsDto.builder()
                    .totalBranches(0).activeVaults(0).activeAlarms(0)
                    .onlineDevices(0).totalDevices(0).activeUsers(0)
                    .todayActivities(0).mqttConnections(0)
                    .serverStatus("error")
                    .build();
        }
    }

    public List<VaultMonitorDto> getAllVaultMonitors() {
        log.debug("Getting all vault monitors");
        try {
            return vaultRepository.findAll().stream()
                    .map(this::mapVaultToMonitorDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting vault monitors", e);
            return List.of();
        }
    }

    public VaultMonitorDto getVaultMonitorById(String id) {
        log.debug("Getting vault monitor by id: {}", id);
        try {
            UUID uuid = UUID.fromString(id);
            Vault vault = vaultRepository.findById(uuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Vault not found: " + id));
            return mapVaultToMonitorDto(vault);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid vault ID: " + id);
        }
    }

    public List<BranchActivityDto> getBranchActivity(Instant from, Instant to, int limit) {
        Instant end = to != null ? to : Instant.now();
        Instant start = from != null ? from : end.minus(7, ChronoUnit.DAYS);

        List<VaultAccessLog> accessLogs = vaultAccessLogRepository.findAll().stream()
                .filter(log -> log.getCreatedAt() != null)
                .filter(log -> !log.getCreatedAt().isBefore(start) && !log.getCreatedAt().isAfter(end))
                .toList();
        List<AlarmLog> alarmLogs = alarmLogRepository.findAll().stream()
                .filter(log -> log.getCreatedAt() != null)
                .filter(log -> !log.getCreatedAt().isBefore(start) && !log.getCreatedAt().isAfter(end))
                .toList();

        Map<UUID, Long> accessByBranch = accessLogs.stream()
                .filter(log -> log.getVault() != null && log.getVault().getBranch() != null)
                .collect(Collectors.groupingBy(log -> log.getVault().getBranch().getId(), Collectors.counting()));
        Map<UUID, Long> alarmsByBranch = alarmLogs.stream()
                .filter(log -> log.getVault() != null && log.getVault().getBranch() != null)
                .collect(Collectors.groupingBy(log -> log.getVault().getBranch().getId(), Collectors.counting()));

        return branchRepository.findAll().stream()
                .map(branch -> BranchActivityDto.builder()
                        .branchId(branch.getId())
                        .branchCode(branch.getCode())
                        .branchName(branch.getName())
                        .accessCount(accessByBranch.getOrDefault(branch.getId(), 0L))
                        .alarmCount(alarmsByBranch.getOrDefault(branch.getId(), 0L))
                        .build())
                .sorted(Comparator.comparingLong(BranchActivityDto::getAccessCount).reversed())
                .limit(Math.max(1, limit))
                .toList();
    }

    public List<VaultStatusSummaryDto> getVaultStatusSummary() {
        Map<String, Long> counts = vaultRepository.findAll().stream()
                .collect(Collectors.groupingBy(v -> v.getStatus() != null ? v.getStatus().name() : "UNKNOWN", Collectors.counting()));
        return counts.entrySet().stream()
                .map(entry -> VaultStatusSummaryDto.builder()
                        .status(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(VaultStatusSummaryDto::getStatus))
                .toList();
    }

    public List<RealtimeStatsDto> getRealtimeStats(int hours) {
        int rangeHours = Math.max(1, Math.min(hours, 168));
        Instant end = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant start = end.minus(rangeHours - 1L, ChronoUnit.HOURS);
        ZoneId zone = ZoneId.systemDefault();

        Map<Instant, Long> eventsByHour = activityLogRepository.findAll().stream()
                .filter(log -> log.getCreatedAt() != null)
                .map(ActivityLog::getCreatedAt)
                .map(ts -> ts.truncatedTo(ChronoUnit.HOURS))
                .filter(ts -> !ts.isBefore(start) && !ts.isAfter(end))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<Instant, Long> alarmsByHour = alarmLogRepository.findAll().stream()
                .filter(log -> log.getCreatedAt() != null)
                .map(AlarmLog::getCreatedAt)
                .map(ts -> ts.truncatedTo(ChronoUnit.HOURS))
                .filter(ts -> !ts.isBefore(start) && !ts.isAfter(end))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<RealtimeStatsDto> data = new ArrayList<>();
        for (int i = 0; i < rangeHours; i++) {
            Instant bucket = start.plus(i, ChronoUnit.HOURS);
            data.add(RealtimeStatsDto.builder()
                    .timestamp(bucket)
                    .time(java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(bucket.atZone(zone)))
                    .events(eventsByHour.getOrDefault(bucket, 0L))
                    .alarms(alarmsByHour.getOrDefault(bucket, 0L))
                    .build());
        }
        return data;
    }

    public List<ServerMetricDto> getServerMetrics(Instant from, Instant to, int limit) {
        List<ServerMonitoring> metrics;
        if (from != null && to != null) {
            metrics = serverMonitoringRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(from, to);
        } else {
            metrics = serverMonitoringRepository.findByOrderByCreatedAtDesc(
                    PageRequest.of(0, Math.max(1, limit), Sort.by(Sort.Direction.DESC, "createdAt")))
                    .stream()
                    .sorted(Comparator.comparing(ServerMonitoring::getCreatedAt))
                    .toList();
        }
        return metrics.stream().map(this::mapServerMetric).toList();
    }

    public ServerHealthDto getServerHealth() {
        return serverMonitoringRepository.findTopByOrderByCreatedAtDesc()
                .map(metric -> ServerHealthDto.builder()
                        .status(Boolean.FALSE.equals(metric.getMqttConnected()) ? "DEGRADED" : "UP")
                        .cpuUsage(metric.getCpuLoad())
                        .memoryUsage(metric.getMemoryLoad())
                        .diskUsage(metric.getDiskLoad())
                        .mqttConnected(metric.getMqttConnected())
                        .websocketSessions(metric.getWebsocketCount())
                        .lastCheckedAt(metric.getCreatedAt())
                        .services(List.of(
                                ServerHealthDto.ServiceStatusDto.builder().name("Backend API").status("UP").description("Spring Boot API").build(),
                                ServerHealthDto.ServiceStatusDto.builder().name("MQTT Broker").status(Boolean.TRUE.equals(metric.getMqttConnected()) ? "UP" : "DOWN").description("MQTT connection").build(),
                                ServerHealthDto.ServiceStatusDto.builder().name("WebSocket").status("UP").description((metric.getWebsocketCount() != null ? metric.getWebsocketCount() : 0) + " active sessions").build()
                        ))
                        .build())
                .orElse(ServerHealthDto.builder()
                        .status("UNKNOWN")
                        .mqttConnected(false)
                        .websocketSessions(0)
                        .services(List.of())
                        .build());
    }

    public BranchUtilizationResponse getBranchUtilization(Instant from, Instant to, Integer days, int limit) {
        Instant end = to != null ? to : Instant.now();
        Instant start = from != null ? from : end.minus(days != null ? Math.max(1, days) : 7, ChronoUnit.DAYS);
        ZoneId zone = ZoneId.systemDefault();

        List<VaultAccessLog> accessLogs = vaultAccessLogRepository.findAll().stream()
                .filter(log -> log.getCreatedAt() != null)
                .filter(log -> !log.getCreatedAt().isBefore(start) && !log.getCreatedAt().isAfter(end))
                .toList();
        List<AlarmLog> alarmLogs = alarmLogRepository.findAll().stream()
                .filter(log -> log.getCreatedAt() != null)
                .filter(log -> !log.getCreatedAt().isBefore(start) && !log.getCreatedAt().isAfter(end))
                .toList();
        List<VaultSession> sessions = vaultSessionRepository.findAll().stream()
                .filter(session -> session.getOpenedAt() != null)
                .filter(session -> !session.getOpenedAt().isBefore(start) && !session.getOpenedAt().isAfter(end))
                .toList();
        List<Vault> vaults = vaultRepository.findAll();

        Map<UUID, Long> accessByBranch = accessLogs.stream()
                .filter(log -> log.getVault() != null && log.getVault().getBranch() != null)
                .collect(Collectors.groupingBy(log -> log.getVault().getBranch().getId(), Collectors.counting()));
        Map<UUID, Long> alarmByBranch = alarmLogs.stream()
                .filter(log -> log.getVault() != null && log.getVault().getBranch() != null)
                .collect(Collectors.groupingBy(log -> log.getVault().getBranch().getId(), Collectors.counting()));
        Map<UUID, Double> avgDurationByBranch = sessions.stream()
                .filter(session -> session.getVault() != null && session.getVault().getBranch() != null && session.getDurationSeconds() != null)
                .collect(Collectors.groupingBy(
                        session -> session.getVault().getBranch().getId(),
                        Collectors.averagingDouble(session -> session.getDurationSeconds() / 60.0)
                ));
        Map<UUID, Long> totalVaultByBranch = vaults.stream()
                .filter(vault -> vault.getBranch() != null)
                .collect(Collectors.groupingBy(vault -> vault.getBranch().getId(), Collectors.counting()));
        Map<UUID, Long> activeVaultByBranch = vaults.stream()
                .filter(vault -> vault.getBranch() != null && vault.isActive())
                .collect(Collectors.groupingBy(vault -> vault.getBranch().getId(), Collectors.counting()));
        Map<UUID, Instant> lastActivityByBranch = accessLogs.stream()
                .filter(log -> log.getVault() != null && log.getVault().getBranch() != null)
                .collect(Collectors.groupingBy(
                        log -> log.getVault().getBranch().getId(),
                        Collectors.mapping(VaultAccessLog::getCreatedAt, Collectors.maxBy(Comparator.naturalOrder()))
                )).entrySet().stream()
                .filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().orElse(null)));

        List<BranchUtilizationResponse.BranchUtilizationItem> branches = branchRepository.findAll().stream()
                .map(branch -> BranchUtilizationResponse.BranchUtilizationItem.builder()
                        .branchId(branch.getId())
                        .branchCode(branch.getCode())
                        .branchName(branch.getName())
                        .accessCount(accessByBranch.getOrDefault(branch.getId(), 0L))
                        .alarmCount(alarmByBranch.getOrDefault(branch.getId(), 0L))
                        .averageDurationMinutes(round(avgDurationByBranch.getOrDefault(branch.getId(), 0.0)))
                        .activeVaultCount(activeVaultByBranch.getOrDefault(branch.getId(), 0L))
                        .totalVaultCount(totalVaultByBranch.getOrDefault(branch.getId(), 0L))
                        .lastActivityAt(lastActivityByBranch.get(branch.getId()))
                        .build())
                .sorted(Comparator.comparingLong(BranchUtilizationResponse.BranchUtilizationItem::getAccessCount).reversed())
                .limit(Math.max(1, limit))
                .toList();

        Map<LocalDate, Long> accessTrend = accessLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getCreatedAt().atZone(zone).toLocalDate(), Collectors.counting()));
        Map<LocalDate, Long> alarmTrend = alarmLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getCreatedAt().atZone(zone).toLocalDate(), Collectors.counting()));
        LocalDate startDate = start.atZone(zone).toLocalDate();
        LocalDate endDate = end.atZone(zone).toLocalDate();
        List<BranchUtilizationResponse.BranchUtilizationTrend> weeklyTrend = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            weeklyTrend.add(BranchUtilizationResponse.BranchUtilizationTrend.builder()
                    .date(date)
                    .accessCount(accessTrend.getOrDefault(date, 0L))
                    .alarmCount(alarmTrend.getOrDefault(date, 0L))
                    .build());
        }

        List<BranchUtilizationResponse.BranchUtilizationStatus> statusDistribution = vaults.stream()
                .collect(Collectors.groupingBy(vault -> vault.getStatus() != null ? vault.getStatus().name() : "UNKNOWN", Collectors.counting()))
                .entrySet().stream()
                .map(entry -> BranchUtilizationResponse.BranchUtilizationStatus.builder()
                        .status(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(BranchUtilizationResponse.BranchUtilizationStatus::getStatus))
                .toList();

        long totalAccess = accessLogs.size();
        long totalAlarms = alarmLogs.size();
        double avgDuration = round(sessions.stream()
                .filter(session -> session.getDurationSeconds() != null)
                .mapToDouble(session -> session.getDurationSeconds() / 60.0)
                .average().orElse(0));
        long activeBranches = branches.stream().filter(branch -> branch.getAccessCount() > 0 || branch.getActiveVaultCount() > 0).count();

        return BranchUtilizationResponse.builder()
                .branches(branches)
                .weeklyTrend(weeklyTrend)
                .statusDistribution(statusDistribution)
                .summary(BranchUtilizationResponse.BranchUtilizationSummary.builder()
                        .totalAccess(totalAccess)
                        .averageDurationMinutes(avgDuration)
                        .totalAlarms(totalAlarms)
                        .activeBranches(activeBranches)
                        .totalBranches(branchRepository.count())
                        .build())
                .build();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private ServerMetricDto mapServerMetric(ServerMonitoring metric) {
        return ServerMetricDto.builder()
                .timestamp(metric.getCreatedAt())
                .cpuUsage(metric.getCpuLoad())
                .memoryUsage(metric.getMemoryLoad())
                .diskUsage(metric.getDiskLoad())
                .mqttConnected(metric.getMqttConnected())
                .websocketSessions(metric.getWebsocketCount())
                .queueSize(metric.getQueueSize())
                .build();
    }

    private VaultMonitorDto mapVaultToMonitorDto(Vault vault) {
        Branch branch = vault.getBranch();
        return VaultMonitorDto.builder()
                .id(vault.getId() != null ? vault.getId().toString() : "")
                .branchId(branch != null && branch.getId() != null ? branch.getId().toString() : "")
                .branchName(branch != null ? branch.getName() : "Unknown")
                .branchCode(branch != null ? branch.getCode() : "")
                .vaultName(vault.getName() != null ? vault.getName() : "Vault")
                .status(vault.getStatus() != null ? vault.getStatus().name().toLowerCase() : "closed")
                .deviceStatus("online")
                .alarmStatus(null)
                .currentUser(null)
                .entryTime(null)
                .duration(null)
                .temperature(0.0)
                .humidity(0.0)
                .lastActivity(vault.getUpdatedAt() != null ? vault.getUpdatedAt() : Instant.now())
                .streamUrl(null)
                .snapshotUrl(null)
                .build();
    }
}
