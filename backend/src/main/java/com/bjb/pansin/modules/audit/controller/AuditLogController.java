package com.bjb.pansin.modules.audit.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.dto.PageResponse;
import com.bjb.pansin.modules.audit.entity.AuditLog;
import com.bjb.pansin.modules.audit.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Audit Logs")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AuditLog>>> list(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @ParameterObject Pageable pageable) {

        var page = actorId != null
                ? repository.findByActorId(actorId, pageable)
                : (entityType != null && entityId != null
                    ? repository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                    : repository.findAll(pageable));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page)));
    }
}
