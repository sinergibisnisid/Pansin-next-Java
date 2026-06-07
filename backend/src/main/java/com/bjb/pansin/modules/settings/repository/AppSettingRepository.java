package com.bjb.pansin.modules.settings.repository;

import com.bjb.pansin.modules.settings.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppSettingRepository extends JpaRepository<AppSetting, UUID> {
    Optional<AppSetting> findByKey(String key);
    List<AppSetting> findByPublicSettingTrue();
}
