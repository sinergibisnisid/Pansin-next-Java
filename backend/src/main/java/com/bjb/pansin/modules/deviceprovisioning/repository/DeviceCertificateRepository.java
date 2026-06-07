package com.bjb.pansin.modules.deviceprovisioning.repository;

import com.bjb.pansin.modules.deviceprovisioning.entity.DeviceCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceCertificateRepository extends JpaRepository<DeviceCertificate, UUID> {
    List<DeviceCertificate> findByDeviceIdOrderByCreatedAtDesc(UUID deviceId);
    List<DeviceCertificate> findByStatusOrderByCreatedAtDesc(String status);
}
