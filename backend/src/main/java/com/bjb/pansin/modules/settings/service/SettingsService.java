package com.bjb.pansin.modules.settings.service;

import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.modules.settings.dto.SettingsRequest;
import com.bjb.pansin.modules.settings.dto.SettingsResponse;
import com.bjb.pansin.modules.settings.entity.AppSetting;
import com.bjb.pansin.modules.settings.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final AppSettingRepository repository;

    @Transactional(readOnly = true)
    public List<SettingsResponse> getAll() {
        return repository.findAll().stream()
                .map(SettingsResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SettingsResponse> getPublicSettings() {
        return repository.findByPublicSettingTrue().stream()
                .map(SettingsResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SettingsResponse getByKey(String key) {
        return repository.findByKey(key)
                .map(SettingsResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Setting", key));
    }

    @Transactional
    public SettingsResponse update(String key, SettingsRequest request) {
        AppSetting setting = repository.findByKey(key)
                .orElseGet(() -> AppSetting.builder().key(key).build());
        setting.setValue(request.getValue());
        if (request.getDescription() != null) setting.setDescription(request.getDescription());
        if (request.getPublicSetting() != null) setting.setPublicSetting(request.getPublicSetting());
        return SettingsResponse.from(repository.save(setting));
    }
}
