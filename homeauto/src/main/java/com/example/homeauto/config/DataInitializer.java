package com.example.homeauto.config;

import com.example.homeauto.model.ApprovalStatus;
import com.example.homeauto.model.AppUser;
import com.example.homeauto.model.Role;
import com.example.homeauto.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (!userRepository.existsByEmail(
                    "admin@homeauto.com")) {

                AppUser admin = new AppUser();

                admin.setFullName("System Administrator");
                admin.setEmail("admin@homeauto.com");

                admin.setPassword(
                        passwordEncoder.encode("admin123")
                );

                admin.setRole(Role.ADMIN);
                admin.setApprovalStatus(
                        ApprovalStatus.APPROVED
                );

                admin.setEnabled(true);

                userRepository.save(admin);

                System.out.println(
                        "===================================="
                );

                System.out.println(
                        "HOMEAUTO ADMIN CREATED"
                );

                System.out.println(
                        "Email: admin@homeauto.com"
                );

                System.out.println(
                        "Password: admin123"
                );

                System.out.println(
                        "===================================="
                );
            }
        };
    }
}