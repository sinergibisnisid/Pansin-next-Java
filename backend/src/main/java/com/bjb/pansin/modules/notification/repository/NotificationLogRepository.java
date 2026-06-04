package com.bjb.pansin.modules.notification.repository;

import com.bjb.pansin.modules.notification.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {
    Page<NotificationLog> findByStatus(String status, Pageable pageable);
}
