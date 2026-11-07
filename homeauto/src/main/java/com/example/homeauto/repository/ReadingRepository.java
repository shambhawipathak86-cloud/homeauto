package com.example.homeauto.repository;

import com.example.homeauto.model.AppUser;
import com.example.homeauto.model.EnvironmentReading;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadingRepository
        extends JpaRepository<EnvironmentReading, Long> {

    List<EnvironmentReading>
    findTop20ByOwnerOrderByRecordedAtDesc(AppUser owner);

    List<EnvironmentReading>
    findByOwner(AppUser owner);

    long countByOwner(AppUser owner);
}