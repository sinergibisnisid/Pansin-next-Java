package com.bjb.pansin.modules.settings.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.modules.settings.dto.SettingsRequest;
import com.bjb.pansin.modules.settings.dto.SettingsResponse;
import com.bjb.pansin.modules.settings.service.SettingsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Settings")
@RestController
@RequestMapping(AppConstants.API_PREFIX)
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/settings")
    @PreAuthorize("hasAuthority('SETTINGS_VIEW') or hasAuthority('SETTINGS_MANAGE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SettingsResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(settingsService.getAll()));
    }

    @GetMapping("/settings/{key}")
    @PreAuthorize("hasAuthority('SETTINGS_VIEW') or hasAuthority('SETTINGS_MANAGE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SettingsResponse>> get(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.ok(settingsService.getByKey(key)));
    }

    @PutMapping("/settings/{key}")
    @PreAuthorize("hasAuthority('SETTINGS_MANAGE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SettingsResponse>> update(@PathVariable String key,
                                                                 @Valid @RequestBody SettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Setting updated", settingsService.update(key, request)));
    }

    @GetMapping("/public/settings")
    public ResponseEntity<ApiResponse<List<SettingsResponse>>> publicSettings() {
        return ResponseEntity.ok(ApiResponse.ok(settingsService.getPublicSettings()));
    }
}
