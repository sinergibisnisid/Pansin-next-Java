package com.bjb.pansin.modules.user.repository;

import com.bjb.pansin.modules.user.entity.WorkingTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkingTimeRepository extends JpaRepository<WorkingTime, UUID> {
    List<WorkingTime> findByUserIdAndActiveTrue(UUID userId);
}
