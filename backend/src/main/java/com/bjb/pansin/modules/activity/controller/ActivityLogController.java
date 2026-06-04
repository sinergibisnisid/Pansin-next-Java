package com.bjb.pansin.modules.activity.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.dto.PageResponse;
import com.bjb.pansin.modules.activity.entity.ActivityLog;
import com.bjb.pansin.modules.activity.repository.ActivityLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Activity Logs")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ActivityLog>>> list(
            @RequestParam(required = false) UUID userId,
            @ParameterObject Pageable pageable) {
        var page = userId != null
                ? repository.findByUserId(userId, pageable)
                : repository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page)));
    }
}
