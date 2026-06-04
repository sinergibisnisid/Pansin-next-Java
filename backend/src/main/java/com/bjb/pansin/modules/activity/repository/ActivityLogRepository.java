package com.bjb.pansin.modules.activity.repository;

import com.bjb.pansin.modules.activity.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    Page<ActivityLog> findByUserId(UUID userId, Pageable pageable);
}
