package com.bjb.pansin.modules.device.repository;

import com.bjb.pansin.common.enums.DeviceStatus;
import com.bjb.pansin.modules.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Optional<Device> findByDeviceCode(String deviceCode);
    List<Device> findByBranchId(UUID branchId);
    List<Device> findByStatus(DeviceStatus status);
    List<Device> findByLastHeartbeatBefore(Instant threshold);
    boolean existsByDeviceCode(String deviceCode);
}
