// ========== MetricsAggregationService.java ==========
package com.banking.analytics.service;

import com.banking.analytics.model.UserMetrics;
import com.banking.analytics.repository.UserMetricsRepository;
import com.banking.analytics.repository.TransactionMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MetricsAggregationService {

    private final UserMetricsRepository userMetricsRepository;
    private final TransactionMetricsRepository transactionMetricsRepository;
    private final InfluxDBService influxDBService;

    @Transactional
    public void processTransaction(String userId, BigDecimal amount, String type, String status) {
        LocalDate today = LocalDate.now();

        // Update PostgreSQL aggregations
        UserMetrics metrics = getUserMetricsForDate(userId, today);

        metrics.setTotalTransactions(metrics.getTotalTransactions() + 1);

        if ("SUCCESS".equals(status)) {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                // Outgoing transaction
                metrics.setTotalSpent(metrics.getTotalSpent().add(amount.abs()));
            } else {
                // Incoming transaction
                metrics.setTotalReceived(metrics.getTotalReceived().add(amount));
            }
        } else {
            metrics.setFailedTransactions(metrics.getFailedTransactions() + 1);
        }

        userMetricsRepository.save(metrics);

        // Write to InfluxDB for time-series analysis
        influxDBService.writeTransactionMetric(userId, amount, type, status);

        log.debug("Transaction processed for user {}: {} {}", userId, amount, type);
    }

    @Transactional
    public void updateAccountBalance(String userId, String accountId, BigDecimal balance) {
        LocalDate today = LocalDate.now();

        UserMetrics metrics = getUserMetricsForDate(userId, today);
        metrics.setAccountBalance(balance);
        userMetricsRepository.save(metrics);

        // Write to InfluxDB
        influxDBService.writeBalanceMetric(userId, accountId, balance);

        log.debug("Balance updated for user {}: {}", userId, balance);
    }

    @Transactional
    public void trackAccountCreation(String userId, String accountId) {
        log.info("Account created for user {}: {}", userId, accountId);
        influxDBService.writeAccountCreationEvent(userId, accountId);
    }

    @Transactional
    public void trackLogin(String userId) {
        LocalDate today = LocalDate.now();

        UserMetrics metrics = getUserMetricsForDate(userId, today);
        metrics.setLoginCount(metrics.getLoginCount() + 1);
        userMetricsRepository.save(metrics);

        log.debug("Login tracked for user {}", userId);
    }

    @Transactional
    public void trackCryptoTransaction(String userId, BigDecimal fiatAmount, String type) {
        LocalDate today = LocalDate.now();

        UserMetrics metrics = getUserMetricsForDate(userId, today);
        BigDecimal currentCryptoValue = metrics.getCryptoValue() != null ?
                metrics.getCryptoValue() : BigDecimal.ZERO;

        if ("BUY".equals(type)) {
            metrics.setCryptoValue(currentCryptoValue.add(fiatAmount));
        } else {
            metrics.setCryptoValue(currentCryptoValue.subtract(fiatAmount));
        }

        userMetricsRepository.save(metrics);

        log.debug("Crypto transaction tracked for user {}: {} {}", userId, type, fiatAmount);
    }

    @Transactional
    public void trackNotification(String userId) {
        LocalDate today = LocalDate.now();

        UserMetrics metrics = getUserMetricsForDate(userId, today);
        metrics.setNotificationsSent(metrics.getNotificationsSent() + 1);
        userMetricsRepository.save(metrics);
    }

    public void aggregateDailyMetrics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Aggregating daily metrics for {}", yesterday);

        // Aggregate transaction metrics by type
        // This would be a complex query or batch job
        // For now, just log
        log.info("Daily aggregation completed");
    }

    private UserMetrics getUserMetricsForDate(String userId, LocalDate date) {
        Optional<UserMetrics> existing = userMetricsRepository
                .findByUserIdAndMetricDate(userId, date);

        return existing.orElseGet(() -> UserMetrics.builder()
                .userId(userId)
                .metricDate(date)
                .totalTransactions(0)
                .totalSpent(BigDecimal.ZERO)
                .totalReceived(BigDecimal.ZERO)
                .accountBalance(BigDecimal.ZERO)
                .cryptoValue(BigDecimal.ZERO)
                .loginCount(0)
                .failedTransactions(0)
                .notificationsSent(0)
                .build());
    }
}


