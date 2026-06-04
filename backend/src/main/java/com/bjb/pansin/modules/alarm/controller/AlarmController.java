package com.bjb.pansin.modules.alarm.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.dto.PageResponse;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.common.security.SecurityUtils;
import com.bjb.pansin.modules.alarm.entity.AlarmLog;
import com.bjb.pansin.modules.alarm.repository.AlarmLogRepository;
import com.bjb.pansin.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "Alarms")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmLogRepository alarmLogRepository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('ALARM_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AlarmLog>>> list(
            @RequestParam(required = false) Boolean unacknowledgedOnly,
            @ParameterObject Pageable pageable) {
        var page = Boolean.TRUE.equals(unacknowledgedOnly)
                ? alarmLogRepository.findByAcknowledgedFalse(pageable)
                : alarmLogRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page)));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAuthority('ALARM_ACK') or hasRole('SUPER_ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<AlarmLog>> acknowledge(@PathVariable UUID id) {
        AlarmLog alarm = alarmLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alarm", id));
        if (alarm.isAcknowledged()) {
            return ResponseEntity.ok(ApiResponse.ok("Already acknowledged", alarm));
        }
        alarm.setAcknowledged(true);
        alarm.setAcknowledgedAt(Instant.now());
        SecurityUtils.getCurrentUserId().flatMap(userRepository::findById).ifPresent(alarm::setAcknowledgedBy);
        return ResponseEntity.ok(ApiResponse.ok("Alarm acknowledged", alarmLogRepository.save(alarm)));
    }
}
