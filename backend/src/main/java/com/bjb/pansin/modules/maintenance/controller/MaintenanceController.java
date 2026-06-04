package com.bjb.pansin.modules.maintenance.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.dto.PageResponse;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.common.security.SecurityUtils;
import com.bjb.pansin.modules.device.repository.DeviceRepository;
import com.bjb.pansin.modules.maintenance.dto.MaintenanceLogRequest;
import com.bjb.pansin.modules.maintenance.dto.MaintenancePlanRequest;
import com.bjb.pansin.modules.maintenance.entity.MaintenanceLog;
import com.bjb.pansin.modules.maintenance.entity.MaintenancePlan;
import com.bjb.pansin.modules.maintenance.repository.MaintenanceLogRepository;
import com.bjb.pansin.modules.maintenance.repository.MaintenancePlanRepository;
import com.bjb.pansin.modules.user.repository.UserRepository;
import com.bjb.pansin.modules.vault.repository.VaultRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Tag(name = "Maintenance")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenancePlanRepository planRepository;
    private final MaintenanceLogRepository logRepository;
    private final VaultRepository vaultRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    // -------- plans --------

    @GetMapping("/plans")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<MaintenancePlan>>> listPlans(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(planRepository.findAll(pageable))));
    }

    @PostMapping("/plans")
    @PreAuthorize("hasAuthority('MAINTENANCE_MANAGE') or hasRole('SUPER_ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<MaintenancePlan>> createPlan(@Valid @RequestBody MaintenancePlanRequest req) {
        MaintenancePlan plan = MaintenancePlan.builder()
                .vault(req.getVaultId() != null ? vaultRepository.findById(req.getVaultId()).orElse(null) : null)
                .device(req.getDeviceId() != null ? deviceRepository.findById(req.getDeviceId()).orElse(null) : null)
                .type(req.getType()).name(req.getName()).description(req.getDescription())
                .intervalDays(req.getIntervalDays())
                .nextDueAt(req.getNextDueAt() != null ? req.getNextDueAt()
                        : Instant.now().plus(req.getIntervalDays(), ChronoUnit.DAYS))
                .active(true).build();
        return ResponseEntity.ok(ApiResponse.ok("Plan created", planRepository.save(plan)));
    }

    @PutMapping("/plans/{id}")
    @PreAuthorize("hasAuthority('MAINTENANCE_MANAGE') or hasRole('SUPER_ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<MaintenancePlan>> updatePlan(@PathVariable UUID id,
                                                                   @Valid @RequestBody MaintenancePlanRequest req) {
        MaintenancePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenancePlan", id));
        plan.setType(req.getType());
        plan.setName(req.getName());
        plan.setDescription(req.getDescription());
        plan.setIntervalDays(req.getIntervalDays());
        if (req.getNextDueAt() != null) plan.setNextDueAt(req.getNextDueAt());
        return ResponseEntity.ok(ApiResponse.ok("Plan updated", planRepository.save(plan)));
    }

    @DeleteMapping("/plans/{id}")
    @PreAuthorize("hasAuthority('MAINTENANCE_MANAGE') or hasRole('SUPER_ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable UUID id) {
        MaintenancePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenancePlan", id));
        plan.setActive(false);
        plan.setDeletedAt(Instant.now());
        planRepository.save(plan);
        return ResponseEntity.ok(ApiResponse.ok("Plan archived", null));
    }

    // -------- logs --------

    @GetMapping("/logs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MaintenanceLog>>> listLogs() {
        return ResponseEntity.ok(ApiResponse.ok(logRepository.findAll()));
    }

    @PostMapping("/logs")
    @PreAuthorize("hasAuthority('MAINTENANCE_MANAGE') or hasRole('SUPER_ADMIN') or hasRole('MAINTENANCE')")
    @Transactional
    public ResponseEntity<ApiResponse<MaintenanceLog>> recordLog(@Valid @RequestBody MaintenanceLogRequest req) {
        MaintenancePlan plan = req.getPlanId() != null
                ? planRepository.findById(req.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("MaintenancePlan", req.getPlanId()))
                : null;

        MaintenanceLog entry = MaintenanceLog.builder()
                .plan(plan)
                .vault(req.getVaultId() != null ? vaultRepository.findById(req.getVaultId()).orElse(null) : null)
                .device(req.getDeviceId() != null ? deviceRepository.findById(req.getDeviceId()).orElse(null) : null)
                .type(req.getType()).notes(req.getNotes())
                .status(req.getStatus() != null ? req.getStatus() : "COMPLETED")
                .performedAt(Instant.now())
                .build();
        SecurityUtils.getCurrentUserId().flatMap(userRepository::findById).ifPresent(entry::setPerformedBy);
        entry = logRepository.save(entry);

        if (plan != null && "COMPLETED".equals(entry.getStatus())) {
            plan.setLastDoneAt(Instant.now());
            plan.setNextDueAt(Instant.now().plus(plan.getIntervalDays(), ChronoUnit.DAYS));
            planRepository.save(plan);
        }
        return ResponseEntity.ok(ApiResponse.ok("Log recorded", entry));
    }
}
