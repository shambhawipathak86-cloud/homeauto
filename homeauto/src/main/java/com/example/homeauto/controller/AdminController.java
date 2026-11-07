package com.example.homeauto.controller;

import com.example.homeauto.model.ApprovalStatus;
import com.example.homeauto.model.AppUser;
import com.example.homeauto.model.Device;
import com.example.homeauto.model.DeviceCompatibilityStatus;
import com.example.homeauto.model.Role;
import com.example.homeauto.model.SystemSetting;
import com.example.homeauto.repository.DeviceRepository;
import com.example.homeauto.repository.ReadingRepository;
import com.example.homeauto.repository.RuleRepository;
import com.example.homeauto.repository.SettingRepository;
import com.example.homeauto.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final RuleRepository ruleRepository;
    private final ReadingRepository readingRepository;
    private final SettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            UserRepository userRepository,
            DeviceRepository deviceRepository,
            RuleRepository ruleRepository,
            ReadingRepository readingRepository,
            SettingRepository settingRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.ruleRepository = ruleRepository;
        this.readingRepository = readingRepository;
        this.settingRepository = settingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // ADMIN DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        loadDashboardData(model);
        return "admin/dashboard";
    }

    // =========================================================
    // ADD USER
    // =========================================================

    @PostMapping("/users/add")
    public String addUser(@RequestParam String fullName, @RequestParam String email, @RequestParam String password,@RequestParam Role role) {
        if (userRepository.existsByEmail(email)) {
            return "redirect:/admin/dashboard?error=emailExists";
        }
        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setEnabled(true);
        userRepository.save(user);
        return "redirect:/admin/dashboard?success=userAdded";
    }

    // =========================================================
    // EDIT USER PAGE
    // =========================================================

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        AppUser user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        loadDashboardData(model);
        model.addAttribute("editUser", user);
        return "admin/dashboard";
    }

    // =========================================================
    // UPDATE USER
    // =========================================================

    @PostMapping("/users/update/{id}")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam Role role,
            @RequestParam(required = false) String password) {

        AppUser user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        userRepository.findByEmail(email)
                .ifPresent(existingUser -> {

                    if (!existingUser.getId().equals(id)) {
                        throw new RuntimeException(
                                "Email is already registered");
                    }
                });

        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);

        if (password != null &&
                !password.trim().isEmpty()) {

            user.setPassword(
                    passwordEncoder.encode(password));
        }

        userRepository.save(user);

        return "redirect:/admin/dashboard?success=userUpdated";
    }

    // =========================================================
    // DELETE USER - FIXED
    // =========================================================

    @PostMapping("/users/delete/{id}")
    public String deleteUser(
            @PathVariable Long id) {

        try {

            AppUser user = userRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));

            /*
             * IMPORTANT:
             * Delete dependent records first.
             */

            // Delete environment readings
            readingRepository.deleteAll(
                    readingRepository.findByOwner(user)
            );

            // Delete automation rules
            ruleRepository.deleteAll(
                    ruleRepository.findByOwner(user)
            );

            // Delete devices
            deviceRepository.deleteAll(
                    deviceRepository.findByOwner(user)
            );

            /*
             * Delete user-specific settings if any
             */
            List<SystemSetting> userSettings =
                    settingRepository.findByOwner(user);

            if (!userSettings.isEmpty()) {
                settingRepository.deleteAll(userSettings);
            }

            /*
             * Finally delete user
             */
            userRepository.delete(user);

            return "redirect:/admin/dashboard?success=userDeleted";

        } catch (Exception e) {

            e.printStackTrace();

            return "redirect:/admin/dashboard?error=userDeleteFailed";
        }
    }

    // =========================================================
    // APPROVE USER
    // =========================================================

    @PostMapping("/approve/{id}")
    public String approveUser(
            @PathVariable Long id) {

        AppUser user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setApprovalStatus(
                ApprovalStatus.APPROVED);

        user.setEnabled(true);

        userRepository.save(user);

        return "redirect:/admin/dashboard?success=approved";
    }

    // =========================================================
    // REJECT USER
    // =========================================================

    @PostMapping("/reject/{id}")
    public String rejectUser(
            @PathVariable Long id) {

        AppUser user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setApprovalStatus(
                ApprovalStatus.REJECTED);

        user.setEnabled(false);

        userRepository.save(user);

        return "redirect:/admin/dashboard?success=rejected";
    }

    // =========================================================
    // APPROVE DEVICE
    // =========================================================

    @PostMapping("/devices/approve/{id}")
    public String approveDevice(
            @PathVariable Long id) {

        Device device = deviceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Device not found"));

        device.setCompatibilityStatus(
                DeviceCompatibilityStatus.APPROVED);

        deviceRepository.save(device);

        return "redirect:/admin/dashboard?success=deviceApproved";
    }

    // =========================================================
    // REJECT DEVICE
    // =========================================================

    @PostMapping("/devices/reject/{id}")
    public String rejectDevice(
            @PathVariable Long id) {

        Device device = deviceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Device not found"));

        device.setCompatibilityStatus(
                DeviceCompatibilityStatus.REJECTED);

        deviceRepository.save(device);

        return "redirect:/admin/dashboard?success=deviceRejected";
    }

    // =========================================================
    // SAVE SYSTEM SETTING
    // =========================================================

    @PostMapping("/settings/save")
    public String saveSetting(
            @RequestParam String settingKey,
            @RequestParam String settingValue) {

        SystemSetting setting =
                settingRepository
                        .findBySettingKeyAndOwnerIsNull(settingKey)
                        .orElseGet(() -> {

                            SystemSetting newSetting =
                                    new SystemSetting();

                            newSetting.setSettingKey(settingKey);
                            newSetting.setOwner(null);

                            return newSetting;
                        });

        setting.setSettingValue(settingValue);

        settingRepository.save(setting);

        return "redirect:/admin/dashboard?success=settingUpdated";
    }

    // =========================================================
    // DELETE SYSTEM SETTING
    // =========================================================

    @PostMapping("/settings/delete/{id}")
    public String deleteSetting(
            @PathVariable Long id) {

        SystemSetting setting =
                settingRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Setting not found"));

        settingRepository.delete(setting);

        return "redirect:/admin/dashboard?success=settingDeleted";
    }

    // =========================================================
    // LOAD ADMIN DASHBOARD DATA
    // =========================================================

    private void loadDashboardData(Model model) {

        // Users
        List<AppUser> users =
                userRepository.findAll();

        model.addAttribute(
                "users",
                users
        );

        // Homeowner count
        model.addAttribute(
                "homeownerCount",
                userRepository
                        .findByRole(Role.HOMEOWNER)
                        .size()
        );

        // Admin count
        model.addAttribute(
                "adminCount",
                userRepository
                        .findByRole(Role.ADMIN)
                        .size()
        );

        // Pending homeowners
        model.addAttribute(
                "pendingCount",
                userRepository
                        .findByRoleAndApprovalStatus(
                                Role.HOMEOWNER,
                                ApprovalStatus.PENDING
                        )
                        .size()
        );

        // Devices
        List<Device> devices =
                deviceRepository.findAll();

        model.addAttribute(
                "devices",
                devices
        );

        // Pending devices
        List<Device> pendingDevices =
                deviceRepository
                        .findByCompatibilityStatus(
                                DeviceCompatibilityStatus.PENDING
                        );

        model.addAttribute(
                "pendingDevices",
                pendingDevices
        );

        // Pending device count
        model.addAttribute(
                "pendingDeviceCount",
                deviceRepository
                        .countByCompatibilityStatus(
                                DeviceCompatibilityStatus.PENDING
                        )
        );

        // Global settings
        List<SystemSetting> settings =
                settingRepository.findByOwnerIsNull();

        model.addAttribute(
                "settings",
                settings
        );
    }
}