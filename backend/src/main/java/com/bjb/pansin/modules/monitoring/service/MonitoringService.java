package com.bjb.pansin.modules.monitoring.service;

import com.bjb.pansin.common.enums.DeviceStatus;
import com.bjb.pansin.common.enums.VaultStatus;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.modules.activity.repository.ActivityLogRepository;
import com.bjb.pansin.modules.alarm.repository.AlarmLogRepository;
import com.bjb.pansin.modules.branch.entity.Branch;
import com.bjb.pansin.modules.branch.repository.BranchRepository;
import com.bjb.pansin.modules.device.repository.DeviceRepository;
import com.bjb.pansin.modules.monitoring.dto.DashboardStatsDto;
import com.bjb.pansin.modules.monitoring.dto.VaultMonitorDto;
import com.bjb.pansin.modules.user.repository.UserRepository;
import com.bjb.pansin.modules.vault.entity.Vault;
import com.bjb.pansin.modules.vault.repository.VaultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
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
