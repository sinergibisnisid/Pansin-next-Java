package com.bjb.pansin.modules.notification.service;

import com.bjb.pansin.common.enums.NotificationChannel;
import com.bjb.pansin.modules.notification.entity.NotificationConfig;
import com.bjb.pansin.modules.notification.entity.NotificationLog;
import com.bjb.pansin.modules.notification.repository.NotificationConfigRepository;
import com.bjb.pansin.modules.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final List<NotificationGateway> gateways;
    private final NotificationConfigRepository configRepository;
    private final NotificationLogRepository logRepository;

    private Map<NotificationChannel, NotificationGateway> registry;

    private Map<NotificationChannel, NotificationGateway> registry() {
        if (registry == null) {
            EnumMap<NotificationChannel, NotificationGateway> map = new EnumMap<>(NotificationChannel.class);
            gateways.forEach(g -> map.put(g.channel(), g));
            registry = map;
        }
        return registry;
    }

    @Async("notificationExecutor")
    public void send(NotificationChannel channel, String to, String subject, String body) {
        sendInternal(channel, to, subject, body, null, null);
    }

    @Async("notificationExecutor")
    public void send(NotificationChannel channel, String to, String subject, String body,
                     String relatedType, UUID relatedId) {
        sendInternal(channel, to, subject, body, relatedType, relatedId);
    }

    @Async("notificationExecutor")
    public void broadcast(List<NotificationChannel> channels, String to, String subject, String body) {
        for (NotificationChannel c : channels) sendInternal(c, to, subject, body, null, null);
    }

    /**
     * Dispatch by event type using notification_configs as the source of truth for channels and recipients.
     */
    @Async("notificationExecutor")
    public void dispatchByEvent(String eventType, UUID branchId, String subject, String body,
                                String relatedType, UUID relatedId) {
        List<NotificationConfig> configs = branchId != null
                ? configRepository.findByBranchIdAndEventTypeAndActiveTrue(branchId, eventType)
                : configRepository.findByEventTypeAndActiveTrue(eventType);

        if (configs.isEmpty()) {
            log.debug("No notification configs for event {}", eventType);
            return;
        }

        for (NotificationConfig cfg : configs) {
            String message = cfg.getTemplate() != null && !cfg.getTemplate().isBlank()
                    ? renderTemplate(cfg.getTemplate(), subject, body)
                    : body;
            for (String recipient : cfg.getRecipients()) {
                sendInternal(cfg.getChannel(), recipient, subject, message, relatedType, relatedId);
            }
        }
    }

    private void sendInternal(NotificationChannel channel, String to, String subject, String body,
                              String relatedType, UUID relatedId) {
        NotificationLog entry = NotificationLog.builder()
                .channel(channel).recipient(to).subject(subject).body(body)
                .relatedType(relatedType).relatedId(relatedId)
                .status("PENDING").build();
        entry = logRepository.save(entry);

        NotificationGateway gw = registry().get(channel);
        if (gw == null) {
            entry.setStatus("FAILED");
            entry.setErrorMessage("No gateway for channel " + channel);
            logRepository.save(entry);
            return;
        }

        try {
            gw.send(to, subject, body);
            entry.setStatus("SENT");
            entry.setSentAt(Instant.now());
        } catch (Exception ex) {
            log.warn("Notification dispatch failed for {} -> {}: {}", channel, to, ex.getMessage());
            entry.setStatus("FAILED");
            entry.setErrorMessage(ex.getMessage());
        }
        logRepository.save(entry);
    }

    private String renderTemplate(String template, String subject, String body) {
        return template
                .replace("{{subject}}", subject != null ? subject : "")
                .replace("{{body}}", body != null ? body : "");
    }
}
