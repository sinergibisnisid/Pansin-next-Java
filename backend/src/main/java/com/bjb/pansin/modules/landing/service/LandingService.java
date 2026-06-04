package com.bjb.pansin.modules.landing.service;

import com.bjb.pansin.common.enums.DeviceStatus;
import com.bjb.pansin.common.enums.VaultStatus;
import com.bjb.pansin.modules.alarm.repository.AlarmLogRepository;
import com.bjb.pansin.modules.device.repository.DeviceRepository;
import com.bjb.pansin.modules.landing.dto.UtilisasiResponse;
import com.bjb.pansin.modules.vault.repository.VaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LandingService {

    private final VaultRepository vaultRepository;
    private final DeviceRepository deviceRepository;
    private final AlarmLogRepository alarmLogRepository;

    @Cacheable(value = "landing:utilisasi", unless = "#result == null")
    public UtilisasiResponse getUtilisasi() {
        long totalVaults = vaultRepository.count();
        long vaultsOpen = vaultRepository.findByStatus(VaultStatus.OPEN).size();
        long vaultsClosed = vaultRepository.findByStatus(VaultStatus.CLOSED).size();
        
        double utilisasi = totalVaults > 0 
                ? (double) vaultsOpen / totalVaults * 100.0 
                : 0.0;

        Instant last24h = Instant.now().minusSeconds(24 * 3600);
        long alarms = alarmLogRepository.findAll().stream()
                .filter(a -> a.getCreatedAt().isAfter(last24h))
                .count();

        long totalDevices = deviceRepository.count();
        long devicesOnline = deviceRepository.findByStatus(DeviceStatus.ONLINE).size();
        long devicesOffline = deviceRepository.findByStatus(DeviceStatus.OFFLINE).size();

        return UtilisasiResponse.builder()
                .totalVaults(totalVaults)
                .vaultsOpen(vaultsOpen)
                .vaultsClosed(vaultsClosed)
                .utilisasiPercentage(Math.round(utilisasi * 100.0) / 100.0)
                .alarmsLast24Hours(alarms)
                .totalDevices(totalDevices)
                .devicesOnline(devicesOnline)
                .devicesOffline(devicesOffline)
                .build();
    }
}
