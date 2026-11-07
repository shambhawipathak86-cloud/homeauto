package com.example.homeauto.repository;

import com.example.homeauto.model.AppUser;
import com.example.homeauto.model.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RuleRepository extends JpaRepository<AutomationRule, Long> {

    List<AutomationRule> findByOwner(AppUser owner);

    Optional<AutomationRule> findByIdAndOwner(
            Long id,
            AppUser owner
    );

    long countByOwner(AppUser owner);

    // Automation service ke liye
    List<AutomationRule> findByActiveTrue();
}