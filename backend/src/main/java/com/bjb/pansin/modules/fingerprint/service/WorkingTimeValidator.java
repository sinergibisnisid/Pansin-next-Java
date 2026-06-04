package com.bjb.pansin.modules.fingerprint.service;

import com.bjb.pansin.common.exceptions.BusinessException;
import com.bjb.pansin.modules.fingerprint.event.FingerprintScannedEvent;
import com.bjb.pansin.modules.user.entity.WorkingTime;
import com.bjb.pansin.modules.user.repository.WorkingTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkingTimeValidator {

    private final WorkingTimeRepository workingTimeRepository;
    private final ApplicationEventPublisher publisher;

    public void validate(UUID userId, ZoneId zoneId) {
        List<WorkingTime> wts = workingTimeRepository.findByUserIdAndActiveTrue(userId);
        if (wts.isEmpty()) {
            // No working time configured -> allow (or deny based on policy). Allow by default.
            return;
        }

        LocalDateTime now = LocalDateTime.now(zoneId != null ? zoneId : ZoneId.of("Asia/Jakarta"));
        DayOfWeek dow = now.getDayOfWeek();
        // DayOfWeek.MONDAY=1..SUNDAY=7 ; we map Sunday=0..Saturday=6
        int day = dow == DayOfWeek.SUNDAY ? 0 : dow.getValue();

        boolean ok = wts.stream().anyMatch(wt ->
                wt.getDayOfWeek() == day
                        && !now.toLocalTime().isBefore(wt.getStartTime())
                        && !now.toLocalTime().isAfter(wt.getEndTime()));

        if (!ok) {
            throw new BusinessException("OUT_OF_WORKING_TIME", "Outside permitted working hours");
        }
    }

    public void publishScan(UUID deviceId, UUID userId, String templateId, boolean matched) {
        publisher.publishEvent(new FingerprintScannedEvent(
                deviceId, userId, templateId, matched, java.time.Instant.now()));
    }
}
