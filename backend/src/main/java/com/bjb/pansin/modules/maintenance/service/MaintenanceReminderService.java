package com.bjb.pansin.modules.maintenance.service;

import com.bjb.pansin.common.enums.NotificationChannel;
import com.bjb.pansin.modules.maintenance.entity.MaintenancePlan;
import com.bjb.pansin.modules.maintenance.repository.MaintenancePlanRepository;
import com.bjb.pansin.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceReminderService {

    private final MaintenancePlanRepository planRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Jakarta")
    public void dailyReminder() {
        Instant inThreeDays = Instant.now().plusSeconds(3 * 24 * 3600);
        List<MaintenancePlan> due = planRepository.findByActiveTrueAndNextDueAtBefore(inThreeDays);

        for (MaintenancePlan plan : due) {
            String message = "Maintenance reminder: %s (%s) is due on %s"
                    .formatted(plan.getName(), plan.getType(), plan.getNextDueAt());
            log.info(message);
            // recipients should come from notification_configs in real impl
            notificationService.send(NotificationChannel.WEBSOCKET, "maintenance",
                    "Maintenance Reminder", message);
        }
    }
}
