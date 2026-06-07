package com.bjb.pansin.modules.settings.dto;

import com.bjb.pansin.modules.settings.entity.AppSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsResponse {
    private UUID id;
    private String key;
    private Map<String, Object> value;
    private String description;
    private boolean publicSetting;
    private Instant createdAt;
    private Instant updatedAt;

    public static SettingsResponse from(AppSetting setting) {
        return SettingsResponse.builder()
                .id(setting.getId())
                .key(setting.getKey())
                .value(setting.getValue())
                .description(setting.getDescription())
                .publicSetting(setting.isPublicSetting())
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}
