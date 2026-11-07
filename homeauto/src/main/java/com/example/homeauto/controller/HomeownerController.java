package com.example.homeauto.controller;

import com.example.homeauto.model.AppUser;
import com.example.homeauto.model.AutomationRule;
import com.example.homeauto.model.Device;
import com.example.homeauto.model.DeviceCompatibilityStatus;
import com.example.homeauto.model.EnvironmentReading;

import com.example.homeauto.repository.DeviceRepository;
import com.example.homeauto.repository.ReadingRepository;
import com.example.homeauto.repository.RuleRepository;
import com.example.homeauto.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/homeowner")
public class HomeownerController {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final RuleRepository ruleRepository;
    private final ReadingRepository readingRepository;

    public HomeownerController(
            UserRepository userRepository,
            DeviceRepository deviceRepository,
            RuleRepository ruleRepository,
            ReadingRepository readingRepository) {

        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.ruleRepository = ruleRepository;
        this.readingRepository = readingRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        AppUser user =
                getCurrentUser(authentication);

        loadDashboardData(model, user);

        model.addAttribute(
                "editRule",
                null
        );

        return "homeowner/dashboard";
    }

    @PostMapping("/devices/add")
    public String addDevice(
            @RequestParam String name,
            @RequestParam String type,
            @RequestParam(required = false) String room,
            Authentication authentication) {

        AppUser user =
                getCurrentUser(authentication);

        Device device = new Device();

        device.setName(name);
        device.setType(type);
        device.setRoom(room);
        device.setEnabled(true);
        device.setStatus(false);
        device.setAdjustmentLevel(100);

        device.setCompatibilityStatus(
                DeviceCompatibilityStatus.PENDING
        );

        device.setOwner(user);

        deviceRepository.save(device);

        return "redirect:/homeowner/dashboard?success=deviceAdded";
    }

    @PostMapping("/devices/toggle/{id}")
    public String toggleDevice(
            @PathVariable Long id,
            Authentication authentication) {

        AppUser user =
                getCurrentUser(authentication);

        Device device =
                deviceRepository
                        .findByIdAndOwner(id, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Device not found"
                                )
                        );

        if (device.getCompatibilityStatus()
                != DeviceCompatibilityStatus.APPROVED) {

            return "redirect:/homeowner/dashboard?error=deviceNotApproved";
        }

        if (!device.isEnabled()) {

            return "redirect:/homeowner/dashboard?error=deviceDisabled";
        }

        device.setStatus(!device.isStatus());

        deviceRepository.save(device);

        return "redirect:/homeowner/dashboard?success=deviceUpdated";
    }

    @PostMapping("/devices/adjust/{id}")
    public String adjustDevice(
            @PathVariable Long id,
            @RequestParam(name = "level", required = false) Integer level,
            Authentication authentication) {

        AppUser user = getCurrentUser(authentication);

        Device device = deviceRepository.findByIdAndOwner(id, user)
                .orElseThrow(() ->
                        new RuntimeException("Device not found"));

        // Missing slider value
        if (level == null) {
            return "redirect:/homeowner/dashboard?error=missingLevel";
        }

        // Device must be approved
        if (device.getCompatibilityStatus()
                != DeviceCompatibilityStatus.APPROVED) {
            return "redirect:/homeowner/dashboard?error=deviceNotApproved";
        }

        // Device must be enabled
        if (!device.isEnabled()) {
            return "redirect:/homeowner/dashboard?error=deviceDisabled";
        }

        // Limit the value to 0–100
        level = Math.max(0, Math.min(100, level));

        device.setAdjustmentLevel(level);
        device.setStatus(level > 0);

        deviceRepository.save(device);

        return "redirect:/homeowner/dashboard?success=deviceAdjusted";
    }
    @PostMapping("/devices/enable/{id}")
    public String enableDevice(
            @PathVariable Long id,
            Authentication authentication) {

        AppUser user =
                getCurrentUser(authentication);

        Device device =
                deviceRepository
                        .findByIdAndOwner(id, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Device not found"
                                )
                        );

        device.setEnabled(!device.isEnabled());

        if (!device.isEnabled()) {
            device.setStatus(false);
        }

        deviceRepository.save(device);

        return "redirect:/homeowner/dashboard?success=deviceStatusUpdated";
    }

    @PostMapping("/devices/delete/{id}")
    public String deleteDevice(
            @PathVariable Long id,
            Authentication authentication) {

        AppUser user =
                getCurrentUser(authentication);

        Device device =
                deviceRepository
                        .findByIdAndOwner(id, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Device not found"
                                )
                        );

        deviceRepository.delete(device);

        return "redirect:/homeowner/dashboard?success=deviceDeleted";
    }

    @PostMapping("/rules/save")
    public String saveRule(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam String conditionType,
            @RequestParam String conditionValue,
            @RequestParam String actionType,
            @RequestParam String actionValue,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(defaultValue = "false") boolean active,
            Authentication authentication) {

        AppUser user =
                getCurrentUser(authentication);

        AutomationRule rule;

        if (id != null) {

            rule =
                    ruleRepository
                            .findByIdAndOwner(id, user)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Rule not found"
                                    )
                            );

        } else {

            rule = new AutomationRule();

            rule.setOwner(user);
        }

        if (deviceId != null) {

            Device device =
                    deviceRepository
                            .findByIdAndOwner(
                                    deviceId,
                                    user
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Device not found"
                                    )
                            );

            rule.setDevice(device);

        } else {

            rule.setDevice(null);
        }

        rule.setName(name.trim());
        rule.setConditionType(
                conditionType.trim().toUpperCase()
        );
        rule.setConditionValue(
                conditionValue.trim()
        );
        rule.setActionType(
                actionType.trim().toUpperCase()
        );
        rule.setActionValue(
                actionValue.trim()
        );
        rule.setActive(active);

        ruleRepository.save(rule);

        return "redirect:/homeowner/dashboard?success=ruleSaved";
    }

    @GetMapping("/rules/edit/{id}")
    public String editRule(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        AppUser user =
                getCurrentUser(authentication);

        AutomationRule editRule =
                ruleRepository
                        .findByIdAndOwner(id, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Rule not found"
                                )
                        );

        loadDashboardData(model, user);

        model.addAttribute(
                "editRule",
                editRule
        );

        return "homeowner/dashboard";
    }

    @PostMapping("/rules/delete/{id}")
    public String deleteRule(
            @PathVariable Long id,
            Authentication authentication) {

        AppUser user =
                getCurrentUser(authentication);

        AutomationRule rule =
                ruleRepository
                        .findByIdAndOwner(id, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Rule not found"
                                )
                        );

        ruleRepository.delete(rule);

        return "redirect:/homeowner/dashboard?success=ruleDeleted";
    }

    @PostMapping("/rules/toggle/{id}")
    public String toggleRule(
            @PathVariable Long id,
            Authentication authentication) {

        AppUser user =
                getCurrentUser(authentication);

        AutomationRule rule =
                ruleRepository
                        .findByIdAndOwner(id, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Rule not found"
                                )
                        );

        rule.setActive(!rule.isActive());

        ruleRepository.save(rule);

        return "redirect:/homeowner/dashboard?success=ruleUpdated";
    }

    @PostMapping("/environment/add")
    public String addEnvironmentReading(
            @RequestParam Double temperature,
            @RequestParam Double humidity,
            @RequestParam(defaultValue = "true")
            boolean securityStatus,
            Authentication authentication) {

        AppUser user =
                getCurrentUser(authentication);

        EnvironmentReading reading =
                new EnvironmentReading();

        reading.setTemperature(temperature);
        reading.setHumidity(humidity);
        reading.setSecurityStatus(securityStatus);
        reading.setRecordedAt(
                LocalDateTime.now()
        );
        reading.setOwner(user);

        readingRepository.save(reading);

        return "redirect:/homeowner/dashboard?success=readingAdded";
    }

    @GetMapping("/api/environment")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>>
    environmentData(
            Authentication authentication) {

        AppUser user =
                getCurrentUser(authentication);

        List<EnvironmentReading> readings =
                readingRepository
                        .findTop20ByOwnerOrderByRecordedAtDesc(
                                user
                        );

        readings.sort(
                Comparator.comparing(
                        EnvironmentReading::getRecordedAt
                )
        );

        List<Map<String, Object>> result =
                readings.stream()
                        .map(reading -> {

                            Map<String, Object> item =
                                    new HashMap<>();

                            item.put(
                                    "time",
                                    reading.getRecordedAt()
                                            .toLocalTime()
                                            .withSecond(0)
                                            .withNano(0)
                                            .toString()
                            );

                            item.put(
                                    "temperature",
                                    reading.getTemperature()
                            );

                            item.put(
                                    "humidity",
                                    reading.getHumidity()
                            );

                            item.put(
                                    "securityStatus",
                                    reading.isSecurityStatus()
                            );

                            return item;
                        })
                        .toList();

        return ResponseEntity.ok(result);
    }

    private void loadDashboardData(
            Model model,
            AppUser user) {

        List<Device> devices =
                deviceRepository.findByOwner(user);

        List<AutomationRule> rules =
                ruleRepository.findByOwner(user);

        List<EnvironmentReading> readings =
                readingRepository
                        .findTop20ByOwnerOrderByRecordedAtDesc(
                                user
                        );

        readings.sort(
                Comparator.comparing(
                        EnvironmentReading::getRecordedAt
                )
        );

        model.addAttribute("user", user);
        model.addAttribute("devices", devices);
        model.addAttribute("rules", rules);
        model.addAttribute("readings", readings);

        long onDeviceCount =
                devices.stream()
                        .filter(Device::isStatus)
                        .count();

        model.addAttribute(
                "onDeviceCount",
                onDeviceCount
        );

        model.addAttribute(
                "deviceCount",
                devices.size()
        );

        model.addAttribute(
                "ruleCount",
                rules.size()
        );

        if (!readings.isEmpty()) {

            EnvironmentReading latest =
                    readings.get(readings.size() - 1);

            model.addAttribute(
                    "latestTemperature",
                    latest.getTemperature()
            );

            model.addAttribute(
                    "latestHumidity",
                    latest.getHumidity()
            );

            model.addAttribute(
                    "latestSecurity",
                    latest.isSecurityStatus()
            );

        } else {

            model.addAttribute(
                    "latestTemperature",
                    "--"
            );

            model.addAttribute(
                    "latestHumidity",
                    "--"
            );

            model.addAttribute(
                    "latestSecurity",
                    true
            );
        }
    }

    private AppUser getCurrentUser(
            Authentication authentication) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }
}