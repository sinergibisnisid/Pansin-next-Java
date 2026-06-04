package com.bjb.pansin.modules.audit.service;

import com.bjb.pansin.common.security.SecurityUtils;
import com.bjb.pansin.modules.audit.entity.AuditLog;
import com.bjb.pansin.modules.audit.repository.AuditLogRepository;
import com.bjb.pansin.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Async("taskExecutor")
    public void log(String action, String entityType, UUID entityId,
                    String description, Map<String, Object> before, Map<String, Object> after,
                    String ip, String userAgent) {

        AuditLog.AuditLogBuilder builder = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .beforeData(before)
                .afterData(after)
                .ipAddress(ip)
                .userAgent(userAgent);

        SecurityUtils.getCurrentUserId().flatMap(userRepository::findById)
                .ifPresent(u -> builder.actor(u).actorName(u.getFullName()));

        auditLogRepository.save(builder.build());
    }
}
