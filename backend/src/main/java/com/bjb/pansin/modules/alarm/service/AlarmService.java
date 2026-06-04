package com.bjb.pansin.modules.alarm.service;

import com.bjb.pansin.common.enums.AlarmType;
import com.bjb.pansin.common.enums.NotificationChannel;
import com.bjb.pansin.modules.alarm.entity.AlarmLog;
import com.bjb.pansin.modules.alarm.repository.AlarmLogRepository;
import com.bjb.pansin.modules.device.repository.DeviceRepository;
import com.bjb.pansin.modules.notification.service.NotificationService;
import com.bjb.pansin.modules.vault.event.AlarmTriggeredEvent;
import com.bjb.pansin.modules.vault.repository.VaultRepository;
import com.bjb.pansin.modules.vault.repository.VaultSessionRepository;
import com.bjb.pansin.modules.websocket.service.WebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmLogRepository alarmLogRepository;
    private final VaultRepository vaultRepository;
    private final DeviceRepository deviceRepository;
    private final VaultSessionRepository sessionRepository;
    private final NotificationService notificationService;
    private final WebSocketBroadcaster broadcaster;

    @EventListener
    @Transactional
    public void on(AlarmTriggeredEvent ev) {
        AlarmLog entry = AlarmLog.builder()
                .vault(ev.getVaultId() != null ? vaultRepository.findById(ev.getVaultId()).orElse(null) : null)
                .device(ev.getDeviceId() != null ? deviceRepository.findById(ev.getDeviceId()).orElse(null) : null)
                .session(ev.getSessionId() != null ? sessionRepository.findById(ev.getSessionId()).orElse(null) : null)
                .type(ev.getType())
                .severity(ev.getSeverity() != null ? ev.getSeverity() : "HIGH")
                .message(ev.getMessage())
                .build();
        alarmLogRepository.save(entry);

        broadcaster.broadcast(WebSocketBroadcaster.TOPIC_ALARM, ev);

        UUID branchId = entry.getVault() != null && entry.getVault().getBranch() != null
                ? entry.getVault().getBranch().getId() : null;
        notificationService.dispatchByEvent(
                "ALARM_" + ev.getType().name(),
                branchId,
                "PANSIN ALARM: " + ev.getType(),
                ev.getMessage() != null ? ev.getMessage() : "Alarm triggered",
                "alarm", entry.getId());

        log.warn("Alarm triggered: type={} message={}", ev.getType(), ev.getMessage());
    }

    @Transactional
    public AlarmLog raise(UUID vaultId, AlarmType type, String severity, String message) {
        AlarmLog alarm = AlarmLog.builder()
                .vault(vaultId != null ? vaultRepository.findById(vaultId).orElse(null) : null)
                .type(type).severity(severity).message(message).build();
        alarm = alarmLogRepository.save(alarm);

        broadcaster.broadcast(WebSocketBroadcaster.TOPIC_ALARM, alarm);
        return alarm;
    }
}
