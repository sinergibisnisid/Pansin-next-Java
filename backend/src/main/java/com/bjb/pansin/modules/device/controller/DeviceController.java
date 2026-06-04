package com.bjb.pansin.modules.device.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.modules.branch.repository.BranchRepository;
import com.bjb.pansin.modules.device.dto.DeviceRequest;
import com.bjb.pansin.modules.device.entity.Device;
import com.bjb.pansin.modules.device.repository.DeviceRepository;
import com.bjb.pansin.modules.vault.repository.VaultRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Devices")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final BranchRepository branchRepository;
    private final VaultRepository vaultRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Device>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(deviceRepository.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Device>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT') or hasRole('ADMIN_CABANG')")
    public ResponseEntity<ApiResponse<Device>> create(@Valid @RequestBody DeviceRequest req) {
        Device device = Device.builder()
                .branch(branchRepository.findById(req.getBranchId())
                        .orElseThrow(() -> new ResourceNotFoundException("Branch", req.getBranchId())))
                .vault(req.getVaultId() != null ? vaultRepository.findById(req.getVaultId()).orElse(null) : null)
                .deviceCode(req.getDeviceCode())
                .name(req.getName())
                .type(req.getType())
                .ipAddress(req.getIpAddress())
                .macAddress(req.getMacAddress())
                .firmwareVersion(req.getFirmwareVersion())
                .active(true)
                .build();
        return ResponseEntity.ok(ApiResponse.ok("Device created", deviceRepository.save(device)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT') or hasRole('ADMIN_CABANG')")
    public ResponseEntity<ApiResponse<Device>> update(@PathVariable UUID id, @Valid @RequestBody DeviceRequest req) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", id));
        device.setBranch(branchRepository.findById(req.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", req.getBranchId())));
        device.setVault(req.getVaultId() != null ? vaultRepository.findById(req.getVaultId()).orElse(null) : null);
        device.setName(req.getName());
        device.setType(req.getType());
        device.setIpAddress(req.getIpAddress());
        device.setMacAddress(req.getMacAddress());
        device.setFirmwareVersion(req.getFirmwareVersion());
        return ResponseEntity.ok(ApiResponse.ok("Device updated", deviceRepository.save(device)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", id));
        deviceRepository.delete(device);
        return ResponseEntity.ok(ApiResponse.ok("Device deleted", null));
    }
}
