package com.bjb.pansin.modules.notification.dto;

import com.bjb.pansin.common.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationConfigRequest {

    private UUID branchId;

    @NotNull
    private NotificationChannel channel;

    @NotBlank
    private String eventType;

    @NotEmpty
    private List<@NotBlank String> recipients;

    private String template;

    @Builder.Default
    private boolean active = true;
}
