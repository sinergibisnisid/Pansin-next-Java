package com.bjb.pansin.modules.notification.repository;

import com.bjb.pansin.common.enums.NotificationChannel;
import com.bjb.pansin.modules.notification.entity.NotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, UUID> {
    List<NotificationConfig> findByEventTypeAndActiveTrue(String eventType);
    List<NotificationConfig> findByEventTypeAndChannelAndActiveTrue(String eventType, NotificationChannel channel);
    List<NotificationConfig> findByBranchIdAndEventTypeAndActiveTrue(UUID branchId, String eventType);
}
