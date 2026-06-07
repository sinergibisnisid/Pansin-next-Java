package com.bjb.pansin.modules.settings.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsRequest {
    @NotNull
    private Map<String, Object> value;
    private String description;
    private Boolean publicSetting;
}
