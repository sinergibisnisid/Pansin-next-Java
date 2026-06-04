package com.bjb.pansin.modules.maintenance.repository;

import com.bjb.pansin.modules.maintenance.entity.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, UUID> {
}
