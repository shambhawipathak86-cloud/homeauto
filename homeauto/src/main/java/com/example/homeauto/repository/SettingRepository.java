package com.example.homeauto.repository;

import com.example.homeauto.model.AppUser;
import com.example.homeauto.model.SystemSetting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettingRepository
        extends JpaRepository<SystemSetting, Long> {

    List<SystemSetting> findByOwner(AppUser owner);

    Optional<SystemSetting> findBySettingKeyAndOwner(
            String settingKey,
            AppUser owner
    );

    List<SystemSetting> findByOwnerIsNull();

    Optional<SystemSetting> findBySettingKeyAndOwnerIsNull(
            String settingKey
    );
}