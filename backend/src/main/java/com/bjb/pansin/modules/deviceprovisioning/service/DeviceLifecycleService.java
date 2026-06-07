package com.bjb.pansin.modules.deviceprovisioning.service;

import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.common.security.SecurityUtils;
import com.bjb.pansin.modules.device.entity.Device;
import com.bjb.pansin.modules.device.repository.DeviceRepository;
import com.bjb.pansin.modules.deviceprovisioning.dto.DeviceCertificateResponse;
import com.bjb.pansin.modules.deviceprovisioning.dto.DeviceLifecycleStateRequest;
import com.bjb.pansin.modules.deviceprovisioning.dto.DeviceLifecycleStateResponse;
import com.bjb.pansin.modules.deviceprovisioning.entity.DeviceLifecycleState;
import com.bjb.pansin.modules.deviceprovisioning.repository.DeviceCertificateRepository;
import com.bjb.pansin.modules.deviceprovisioning.repository.DeviceLifecycleStateRepository;
import com.bjb.pansin.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceLifecycleService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final DeviceLifecycleStateRepository lifecycleStateRepository;
    private final DeviceCertificateRepository certificateRepository;

    @Transactional(readOnly = true)
    public List<DeviceLifecycleStateResponse> getAllStates(String state) {
        List<DeviceLifecycleState> rows = state != null && !state.isBlank()
                ? lifecycleStateRepository.findByStateOrderByCreatedAtDesc(state)
                : lifecycleStateRepository.findAll();
        return rows.stream().map(DeviceLifecycleStateResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DeviceLifecycleStateResponse> getDeviceStates(UUID deviceId) {
        ensureDeviceExists(deviceId);
        return lifecycleStateRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId).stream()
                .map(DeviceLifecycleStateResponse::from)
                .toList();
    }

    @Transactional
    public DeviceLifecycleStateResponse addDeviceState(UUID deviceId, DeviceLifecycleStateRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));
        String previousState = device.getLifecycleState();
        DeviceLifecycleState lifecycleState = DeviceLifecycleState.builder()
                .device(device)
                .state(request.getState())
                .previousState(previousState)
                .reason(request.getReason())
                .metadata(request.getMetadata())
                .actor(SecurityUtils.getCurrentUserId().flatMap(userRepository::findById).orElse(null))
                .build();
        device.setLifecycleState(request.getState());
        deviceRepository.save(device);
        return DeviceLifecycleStateResponse.from(lifecycleStateRepository.save(lifecycleState));
    }

    @Transactional(readOnly = true)
    public List<DeviceCertificateResponse> getDeviceCertificates(UUID deviceId) {
        ensureDeviceExists(deviceId);
        return certificateRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId).stream()
                .map(DeviceCertificateResponse::from)
                .toList();
    }

    private void ensureDeviceExists(UUID deviceId) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device", deviceId);
        }
    }
}
