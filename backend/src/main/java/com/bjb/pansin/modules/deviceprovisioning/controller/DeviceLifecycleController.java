package com.bjb.pansin.modules.deviceprovisioning.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.modules.deviceprovisioning.dto.DeviceCertificateResponse;
import com.bjb.pansin.modules.deviceprovisioning.dto.DeviceLifecycleStateRequest;
import com.bjb.pansin.modules.deviceprovisioning.dto.DeviceLifecycleStateResponse;
import com.bjb.pansin.modules.deviceprovisioning.service.DeviceLifecycleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Device Lifecycle")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/device-lifecycle")
@RequiredArgsConstructor
public class DeviceLifecycleController {
    private final DeviceLifecycleService service;

    @GetMapping("/states")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DeviceLifecycleStateResponse>>> states(
            @RequestParam(required = false) String state) {
        return ResponseEntity.ok(ApiResponse.ok(service.getAllStates(state)));
    }

    @GetMapping("/devices/{deviceId}/states")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DeviceLifecycleStateResponse>>> deviceStates(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(ApiResponse.ok(service.getDeviceStates(deviceId)));
    }

    @PostMapping("/devices/{deviceId}/states")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT') or hasRole('ADMIN_CABANG')")
    public ResponseEntity<ApiResponse<DeviceLifecycleStateResponse>> addState(
            @PathVariable UUID deviceId,
            @Valid @RequestBody DeviceLifecycleStateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Device lifecycle state updated", service.addDeviceState(deviceId, request)));
    }

    @GetMapping("/devices/{deviceId}/certificates")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DeviceCertificateResponse>>> certificates(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(ApiResponse.ok(service.getDeviceCertificates(deviceId)));
    }
}
