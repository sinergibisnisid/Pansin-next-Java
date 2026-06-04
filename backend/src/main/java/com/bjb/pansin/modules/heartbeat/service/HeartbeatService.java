package com.bjb.pansin.modules.heartbeat.service;

import com.bjb.pansin.common.enums.DeviceStatus;
import com.bjb.pansin.modules.device.entity.Device;
import com.bjb.pansin.modules.device.event.DeviceOfflineEvent;
import com.bjb.pansin.modules.device.repository.DeviceRepository;
import com.bjb.pansin.modules.heartbeat.entity.DeviceHeartbeat;
import com.bjb.pansin.modules.heartbeat.repository.DeviceHeartbeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatService {

    private static final long OFFLINE_THRESHOLD_SECONDS = 90;

    private final DeviceRepository deviceRepository;
    private final DeviceHeartbeatRepository heartbeatRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void recordHeartbeat(UUID deviceId, BigDecimal cpu, BigDecimal mem,
                                Integer signal, Long uptime, Map<String, Object> meta) {
        deviceRepository.findById(deviceId).ifPresentOrElse(device -> {
            device.setStatus(DeviceStatus.ONLINE);
            device.setLastHeartbeat(Instant.now());
            if (signal != null) device.setSignalQuality(signal);
            deviceRepository.save(device);

            heartbeatRepository.save(DeviceHeartbeat.builder()
                    .device(device).cpuLoad(cpu).memoryLoad(mem)
                    .signalQuality(signal).uptimeSeconds(uptime)
                    .metadata(meta).build());
        }, () -> log.warn("Heartbeat for unknown device {}", deviceId));
    }

    @Scheduled(fixedRate = 30_000)
    @Transactional
    public void detectOfflineDevices() {
        Instant threshold = Instant.now().minusSeconds(OFFLINE_THRESHOLD_SECONDS);
        List<Device> stale = deviceRepository.findByLastHeartbeatBefore(threshold);
        for (Device d : stale) {
            if (d.getStatus() == DeviceStatus.ONLINE) {
                d.setStatus(DeviceStatus.OFFLINE);
                deviceRepository.save(d);
                publisher.publishEvent(new DeviceOfflineEvent(d.getId(), d.getDeviceCode(), Instant.now()));
                log.warn("Device {} marked OFFLINE (no heartbeat since {})", d.getDeviceCode(), d.getLastHeartbeat());
            }
        }
    }
}
