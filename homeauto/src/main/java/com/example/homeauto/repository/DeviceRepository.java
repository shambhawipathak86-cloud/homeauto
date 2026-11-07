package com.example.homeauto.repository;

import com.example.homeauto.model.AppUser;
import com.example.homeauto.model.Device;
import com.example.homeauto.model.DeviceCompatibilityStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository
        extends JpaRepository<Device, Long> {

    List<Device> findByOwner(AppUser owner);

    Optional<Device> findByIdAndOwner(
            Long id,
            AppUser owner
    );

    long countByOwner(AppUser owner);

    List<Device> findByCompatibilityStatus(
            DeviceCompatibilityStatus status
    );

    List<Device> findByOwnerAndCompatibilityStatus(
            AppUser owner,
            DeviceCompatibilityStatus status
    );

    long countByCompatibilityStatus(
            DeviceCompatibilityStatus status
    );
}