package com.bjb.pansin.modules.activity.service;

import com.bjb.pansin.common.security.SecurityUtils;
import com.bjb.pansin.modules.activity.entity.ActivityLog;
import com.bjb.pansin.modules.activity.repository.ActivityLogRepository;
import com.bjb.pansin.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repository;
    private final UserRepository userRepository;

    @Async("taskExecutor")
    public void log(String activity, String description, String ip, String userAgent, Map<String, Object> meta) {
        ActivityLog.ActivityLogBuilder b = ActivityLog.builder()
                .activity(activity)
                .description(description)
                .ipAddress(ip)
                .userAgent(userAgent)
                .metadata(meta);

        SecurityUtils.getCurrentUserId().flatMap(userRepository::findById).ifPresent(b::user);
        repository.save(b.build());
    }
}
