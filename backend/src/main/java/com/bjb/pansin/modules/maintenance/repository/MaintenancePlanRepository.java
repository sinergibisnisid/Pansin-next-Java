package com.bjb.pansin.modules.maintenance.repository;

import com.bjb.pansin.modules.maintenance.entity.MaintenanceLog;
import com.bjb.pansin.modules.maintenance.entity.MaintenancePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, UUID> {
    List<MaintenancePlan> findByActiveTrueAndNextDueAtBefore(Instant before);
}
