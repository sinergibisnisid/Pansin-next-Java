package com.bjb.pansin.modules.notification.dto;

import com.bjb.pansin.common.enums.NotificationChannel;
import com.bjb.pansin.modules.notification.entity.NotificationConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationConfigResponse {

    private UUID id;
    private BranchRef branch;
    private NotificationChannel channel;
    private String eventType;
    private List<String> recipients;
    private String template;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static NotificationConfigResponse from(NotificationConfig config) {
        return NotificationConfigResponse.builder()
                .id(config.getId())
                .branch(config.getBranch() != null ? BranchRef.builder()
                        .id(config.getBranch().getId())
                        .code(config.getBranch().getCode())
                        .name(config.getBranch().getName())
                        .build() : null)
                .channel(config.getChannel())
                .eventType(config.getEventType())
                .recipients(config.getRecipients())
                .template(config.getTemplate())
                .active(config.isActive())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchRef {
        private UUID id;
        private String code;
        private String name;
    }
}
