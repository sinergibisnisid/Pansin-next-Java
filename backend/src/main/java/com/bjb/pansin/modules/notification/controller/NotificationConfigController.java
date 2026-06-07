package com.bjb.pansin.modules.notification.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.enums.NotificationChannel;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.modules.branch.repository.BranchRepository;
import com.bjb.pansin.modules.notification.dto.NotificationConfigRequest;
import com.bjb.pansin.modules.notification.dto.NotificationConfigResponse;
import com.bjb.pansin.modules.notification.entity.NotificationConfig;
import com.bjb.pansin.modules.notification.repository.NotificationConfigRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notification Configs")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/notification-configs")
@RequiredArgsConstructor
public class NotificationConfigController {

    private final NotificationConfigRepository repository;
    private final BranchRepository branchRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW') or hasAuthority('NOTIFICATION_MANAGE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationConfigResponse>>> list(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) NotificationChannel channel) {
        List<NotificationConfig> configs;
        if (branchId != null && eventType != null && channel == null) {
            configs = repository.findByBranchIdAndEventTypeAndActiveTrue(branchId, eventType);
        } else if (eventType != null && channel != null) {
            configs = repository.findByEventTypeAndChannelAndActiveTrue(eventType, channel);
        } else if (eventType != null) {
            configs = repository.findByEventTypeAndActiveTrue(eventType);
        } else {
            configs = repository.findAll();
        }

        return ResponseEntity.ok(ApiResponse.ok(configs.stream()
                .map(NotificationConfigResponse::from)
                .toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW') or hasAuthority('NOTIFICATION_MANAGE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<NotificationConfigResponse>> get(@PathVariable UUID id) {
        NotificationConfig config = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationConfig", id));
        return ResponseEntity.ok(ApiResponse.ok(NotificationConfigResponse.from(config)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_MANAGE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<NotificationConfigResponse>> create(@Valid @RequestBody NotificationConfigRequest req) {
        NotificationConfig config = NotificationConfig.builder()
                .branch(req.getBranchId() != null
                        ? branchRepository.findById(req.getBranchId())
                                .orElseThrow(() -> new ResourceNotFoundException("Branch", req.getBranchId()))
                        : null)
                .channel(req.getChannel())
                .eventType(req.getEventType())
                .recipients(req.getRecipients())
                .template(req.getTemplate())
                .active(req.isActive())
                .build();
        return ResponseEntity.ok(ApiResponse.ok("Notification config created",
                NotificationConfigResponse.from(repository.save(config))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION_MANAGE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<NotificationConfigResponse>> update(@PathVariable UUID id,
                                                                           @Valid @RequestBody NotificationConfigRequest req) {
        NotificationConfig config = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationConfig", id));
        config.setBranch(req.getBranchId() != null
                ? branchRepository.findById(req.getBranchId())
                        .orElseThrow(() -> new ResourceNotFoundException("Branch", req.getBranchId()))
                : null);
        config.setChannel(req.getChannel());
        config.setEventType(req.getEventType());
        config.setRecipients(req.getRecipients());
        config.setTemplate(req.getTemplate());
        config.setActive(req.isActive());
        return ResponseEntity.ok(ApiResponse.ok("Notification config updated",
                NotificationConfigResponse.from(repository.save(config))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION_MANAGE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        NotificationConfig config = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationConfig", id));
        repository.delete(config);
        return ResponseEntity.ok(ApiResponse.ok("Notification config deleted", null));
    }
}
