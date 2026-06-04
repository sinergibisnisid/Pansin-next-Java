package com.bjb.pansin.modules.monitoring.service;

import com.bjb.pansin.modules.monitoring.entity.ServerMonitoring;
import com.bjb.pansin.modules.monitoring.repository.ServerMonitoringRepository;
import com.bjb.pansin.modules.mqtt.service.MqttBrokerStatus;
import com.bjb.pansin.modules.websocket.service.WebSocketSessionRegistry;
import com.sun.management.OperatingSystemMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerMonitoringService {

    private final ServerMonitoringRepository repository;
    private final MqttBrokerStatus mqttBrokerStatus;
    private final WebSocketSessionRegistry websocketRegistry;

    @Scheduled(fixedRate = 60_000)
    public void capture() {
        try {
            OperatingSystemMXBean os = (OperatingSystemMXBean)
                    ManagementFactory.getOperatingSystemMXBean();

            BigDecimal cpu = BigDecimal.valueOf(Math.max(os.getCpuLoad() * 100, 0d))
                    .setScale(2, RoundingMode.HALF_UP);

            long total = os.getTotalMemorySize();
            long free = os.getFreeMemorySize();
            BigDecimal mem = total > 0
                    ? BigDecimal.valueOf((total - free) * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal disk = computeDiskLoad();

            String host = "unknown";
            try { host = InetAddress.getLocalHost().getHostName(); } catch (Exception ignored) {}

            repository.save(ServerMonitoring.builder()
                    .hostname(host)
                    .cpuLoad(cpu)
                    .memoryLoad(mem)
                    .diskLoad(disk)
                    .mqttConnected(mqttBrokerStatus.isConnected())
                    .websocketCount(websocketRegistry.activeSessions())
                    .queueSize(0)
                    .build());
        } catch (Exception ex) {
            log.warn("Server metrics capture failed: {}", ex.getMessage());
        }
    }

    private BigDecimal computeDiskLoad() {
        try {
            FileStore store = FileSystems.getDefault().getFileStores().iterator().next();
            long total = store.getTotalSpace();
            long usable = store.getUsableSpace();
            if (total == 0) return BigDecimal.ZERO;
            return BigDecimal.valueOf((total - usable) * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }
}
