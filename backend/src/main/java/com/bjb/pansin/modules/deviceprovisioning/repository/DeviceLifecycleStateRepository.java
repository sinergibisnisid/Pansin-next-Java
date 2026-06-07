package com.bjb.pansin.modules.deviceprovisioning.repository;

import com.bjb.pansin.modules.deviceprovisioning.entity.DeviceLifecycleState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceLifecycleStateRepository extends JpaRepository<DeviceLifecycleState, UUID> {
    List<DeviceLifecycleState> findByDeviceIdOrderByCreatedAtDesc(UUID deviceId);
    List<DeviceLifecycleState> findByStateOrderByCreatedAtDesc(String state);
}
