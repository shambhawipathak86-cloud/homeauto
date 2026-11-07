package com.example.homeauto.service;

import com.example.homeauto.model.AutomationRule;
import com.example.homeauto.model.Device;
import com.example.homeauto.model.DeviceCompatibilityStatus;
import com.example.homeauto.model.EnvironmentReading;

import com.example.homeauto.repository.DeviceRepository;
import com.example.homeauto.repository.ReadingRepository;
import com.example.homeauto.repository.RuleRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class AutomationService {

    private final RuleRepository ruleRepository;
    private final DeviceRepository deviceRepository;
    private final ReadingRepository readingRepository;

    public AutomationService(
            RuleRepository ruleRepository,
            DeviceRepository deviceRepository,
            ReadingRepository readingRepository) {

        this.ruleRepository = ruleRepository;
        this.deviceRepository = deviceRepository;
        this.readingRepository = readingRepository;
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void executeAutomationRules() {

        List<AutomationRule> rules =
                ruleRepository.findByActiveTrue();

        for (AutomationRule rule : rules) {

            try {

                Device device = rule.getDevice();

                if (device == null) {
                    continue;
                }

                if (!device.isEnabled()) {
                    continue;
                }

                if (device.getCompatibilityStatus()
                        != DeviceCompatibilityStatus.APPROVED) {
                    continue;
                }

                if (conditionMatches(rule)) {
                    executeAction(rule, device);
                }

            } catch (Exception e) {

                System.out.println(
                        "Automation error for rule "
                                + rule.getId()
                                + ": "
                                + e.getMessage()
                );
            }
        }
    }

    private boolean conditionMatches(
            AutomationRule rule) {

        String type =
                rule.getConditionType()
                        .trim()
                        .toUpperCase();

        String value =
                rule.getConditionValue()
                        .trim();

        if (type.equals("TEMPERATURE")) {

            return temperatureCondition(
                    rule,
                    value
            );
        }

        if (type.equals("HUMIDITY")) {

            return humidityCondition(
                    rule,
                    value
            );
        }

        if (type.equals("SECURITY")) {

            return securityCondition(
                    rule,
                    value
            );
        }

        if (type.equals("TIME")) {

            return timeCondition(value);
        }

        return false;
    }

    private boolean temperatureCondition(
            AutomationRule rule,
            String value) {

        try {

            double expected =
                    Double.parseDouble(value);

            EnvironmentReading reading =
                    getLatestReading(
                            rule
                    );

            if (reading == null) {
                return false;
            }

            return reading.getTemperature()
                    >= expected;

        } catch (Exception e) {

            return false;
        }
    }

    private boolean humidityCondition(
            AutomationRule rule,
            String value) {

        try {

            double expected =
                    Double.parseDouble(value);

            EnvironmentReading reading =
                    getLatestReading(
                            rule
                    );

            if (reading == null) {
                return false;
            }

            return reading.getHumidity()
                    >= expected;

        } catch (Exception e) {

            return false;
        }
    }

    private boolean securityCondition(
            AutomationRule rule,
            String value) {

        EnvironmentReading reading =
                getLatestReading(rule);

        if (reading == null) {
            return false;
        }

        String normalized =
                value.toUpperCase(Locale.ROOT);

        if (normalized.equals("SECURE")
                || normalized.equals("SAFE")
                || normalized.equals("TRUE")
                || normalized.equals("1")) {

            return reading.isSecurityStatus();
        }

        if (normalized.equals("ALERT")
                || normalized.equals("UNSAFE")
                || normalized.equals("FALSE")
                || normalized.equals("0")) {

            return !reading.isSecurityStatus();
        }

        return false;
    }

    private boolean timeCondition(
            String value) {

        try {

            LocalTime target =
                    LocalTime.parse(
                            value,
                            DateTimeFormatter.ofPattern(
                                    "HH:mm"
                            )
                    );

            LocalTime now =
                    LocalTime.now();

            return now.getHour() == target.getHour()
                    && now.getMinute() == target.getMinute();

        } catch (Exception e) {

            return false;
        }
    }

    private EnvironmentReading getLatestReading(
            AutomationRule rule) {

        if (rule.getOwner() == null) {
            return null;
        }

        List<EnvironmentReading> readings =
                readingRepository
                        .findTop20ByOwnerOrderByRecordedAtDesc(
                                rule.getOwner()
                        );

        if (readings.isEmpty()) {
            return null;
        }

        return readings.get(0);
    }

    private void executeAction(
            AutomationRule rule,
            Device device) {

        String action =
                rule.getActionType()
                        .trim()
                        .toUpperCase();

        if (action.equals("TURN_ON")) {

            device.setStatus(true);

            deviceRepository.save(device);

            return;
        }

        if (action.equals("TURN_OFF")) {

            device.setStatus(false);

            deviceRepository.save(device);

            return;
        }

        if (action.equals("ADJUST")) {

            try {

                int level =
                        Integer.parseInt(
                                rule.getActionValue()
                                        .trim()
                        );

                level =
                        Math.max(
                                0,
                                Math.min(
                                        100,
                                        level
                                )
                        );

                device.setAdjustmentLevel(level);

                device.setStatus(level > 0);

                deviceRepository.save(device);

            } catch (NumberFormatException ignored) {
            }
        }
    }
}