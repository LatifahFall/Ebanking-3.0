// ========== AlertService.java ==========
package com.banking.analytics.service;

import com.banking.analytics.model.Alert;
import com.banking.analytics.model.UserMetrics;
import com.banking.analytics.repository.AlertRepository;
import com.banking.analytics.repository.UserMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserMetricsRepository userMetricsRepository;

    @Transactional
    public void checkLargeTransaction(String userId, BigDecimal amount) {
        // Check if there's already an active alert
        if (hasActiveAlert(userId, Alert.AlertType.LARGE_TRANSACTION)) {
            return;
        }

        Alert alert = Alert.builder()
                .userId(userId)
                .alertType(Alert.AlertType.LARGE_TRANSACTION)
                .severity(Alert.Severity.WARNING)
                .title("Large Transaction Detected")
                .message(String.format("A large transaction of %s was detected", amount))
                .currentValue(amount)
                .thresholdValue(new BigDecimal("1000"))
                .status(Alert.AlertStatus.ACTIVE)
                .notified(false)
                .build();

        alertRepository.save(alert);
        log.info("Large transaction alert created for user {}: {}", userId, amount);
    }

    @Transactional
    public void checkSpendingThreshold(String userId) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        // Get monthly spending
        BigDecimal monthlySpending = userMetricsRepository
                .findByUserIdAndMetricDateBetween(userId, monthStart, today)
                .stream()
                .map(UserMetrics::getTotalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal threshold = new BigDecimal("5000"); // Example threshold

        if (monthlySpending.compareTo(threshold) > 0 &&
                !hasActiveAlert(userId, Alert.AlertType.SPENDING_THRESHOLD)) {

            Alert alert = Alert.builder()
                    .userId(userId)
                    .alertType(Alert.AlertType.SPENDING_THRESHOLD)
                    .severity(Alert.Severity.WARNING)
                    .title("Monthly Spending Threshold Exceeded")
                    .message(String.format("You've spent %s this month, exceeding your threshold of %s",
                            monthlySpending, threshold))
                    .currentValue(monthlySpending)
                    .thresholdValue(threshold)
                    .status(Alert.AlertStatus.ACTIVE)
                    .notified(false)
                    .build();

            alertRepository.save(alert);
            log.info("Spending threshold alert created for user {}", userId);
        }
    }

    @Transactional
    public void triggerLowBalanceAlert(String userId, BigDecimal balance) {
        if (hasActiveAlert(userId, Alert.AlertType.LOW_BALANCE)) {
            return;
        }

        Alert alert = Alert.builder()
                .userId(userId)
                .alertType(Alert.AlertType.LOW_BALANCE)
                .severity(Alert.Severity.CRITICAL)
                .title("Low Balance Alert")
                .message(String.format("Your balance is low: %s", balance))
                .currentValue(balance)
                .thresholdValue(new BigDecimal("100"))
                .status(Alert.AlertStatus.ACTIVE)
                .notified(false)
                .build();

        alertRepository.save(alert);
        log.info("Low balance alert created for user {}: {}", userId, balance);
    }

    @Transactional
    public void checkSuspiciousLogin(String userId) {
        // Count failed logins in the last hour
        // This is simplified - in reality, you'd query time-series data

        if (!hasActiveAlert(userId, Alert.AlertType.SUSPICIOUS_LOGIN)) {
            Alert alert = Alert.builder()
                    .userId(userId)
                    .alertType(Alert.AlertType.SUSPICIOUS_LOGIN)
                    .severity(Alert.Severity.CRITICAL)
                    .title("Suspicious Login Activity")
                    .message("Multiple failed login attempts detected")
                    .status(Alert.AlertStatus.ACTIVE)
                    .notified(false)
                    .build();

            alertRepository.save(alert);
            log.warn("Suspicious login alert created for user {}", userId);
        }
    }

    public List<Alert> getActiveAlerts(String userId) {
        return alertRepository.findByUserIdAndStatus(userId, Alert.AlertStatus.ACTIVE);
    }

    @Transactional
    public void resolveAlert(String alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setStatus(Alert.AlertStatus.RESOLVED);
            alertRepository.save(alert);
            log.info("Alert {} resolved", alertId);
        });
    }

    @Transactional
    public void createFraudAlert(String userId, String alertType, String severity, BigDecimal amount) {
        Alert.Severity alertSeverity = mapSeverity(severity);

        Alert alert = Alert.builder()
                .userId(userId)
                .alertType(Alert.AlertType.UNUSUAL_ACTIVITY)
                .severity(alertSeverity)
                .title("Fraud Detection Alert")
                .message(String.format("Suspicious activity detected: %s - Amount: %s", alertType, amount))
                .currentValue(amount)
                .status(Alert.AlertStatus.ACTIVE)
                .notified(false)
                .build();

        alertRepository.save(alert);
        log.error("FRAUD ALERT created for user {}: {} ({}) - Amount: {}",
                userId, alertType, severity, amount);
    }

    private Alert.Severity mapSeverity(String severity) {
        if (severity == null) return Alert.Severity.WARNING;
        return switch (severity.toUpperCase()) {
            case "HIGH", "CRITICAL" -> Alert.Severity.CRITICAL;
            case "MEDIUM", "WARNING" -> Alert.Severity.WARNING;
            case "LOW", "INFO" -> Alert.Severity.INFO;
            default -> Alert.Severity.WARNING;
        };
    }

    private boolean hasActiveAlert(String userId, Alert.AlertType type) {
        return alertRepository.existsByUserIdAndAlertTypeAndStatus(
                userId, type, Alert.AlertStatus.ACTIVE);
    }
}