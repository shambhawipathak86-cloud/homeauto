package com.example.homeauto.repository;

import com.example.homeauto.model.ApprovalStatus;
import com.example.homeauto.model.AppUser;
import com.example.homeauto.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AppUser> findByRole(Role role);

    List<AppUser> findByRoleAndApprovalStatus(
            Role role,
            ApprovalStatus approvalStatus
    );
}