// ========== DashboardController.java ==========
package com.banking.analytics.controller;

import com.banking.analytics.dto.*;
import com.banking.analytics.model.UserMetrics;
import com.banking.analytics.repository.UserMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final UserMetricsRepository userMetricsRepository;

    @GetMapping("/dashboard/summary")
    @PreAuthorize("#userId == authentication.principal.claims['sub'] or hasRole('ADMIN')")
    public ResponseEntity<DashboardSummary> getDashboardSummary(@RequestParam String userId) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        // Get current month metrics
        List<UserMetrics> monthlyMetrics = userMetricsRepository
                .findByUserIdAndMetricDateBetween(userId, monthStart, today);

        BigDecimal monthlySpending = monthlyMetrics.stream()
                .map(UserMetrics::getTotalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyIncome = monthlyMetrics.stream()
                .map(UserMetrics::getTotalReceived)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalTransactions = monthlyMetrics.stream()
                .mapToInt(UserMetrics::getTotalTransactions)
                .sum();

        // Get latest balance
        BigDecimal currentBalance = monthlyMetrics.stream()
                .sorted((a, b) -> b.getMetricDate().compareTo(a.getMetricDate()))
                .findFirst()
                .map(UserMetrics::getAccountBalance)
                .orElse(BigDecimal.ZERO);

        DashboardSummary summary = DashboardSummary.builder()
                .userId(userId)
                .currentBalance(currentBalance)
                .monthlySpending(monthlySpending)
                .monthlyIncome(monthlyIncome)
                .transactionsThisMonth(totalTransactions)
                .generatedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/spending/breakdown")
    @PreAuthorize("#userId == authentication.principal.claims['sub'] or hasRole('ADMIN')")
    public ResponseEntity<List<CategoryBreakdown>> getSpendingBreakdown(
            @RequestParam String userId,
            @RequestParam(defaultValue = "MONTH") String period) {

        // Simplified - would use SpendingCategory table in reality
        LocalDate today = LocalDate.now();
        LocalDate periodStart = "WEEK".equals(period) ?
                today.minusWeeks(1) : today.minusMonths(1);

        List<UserMetrics> metrics = userMetricsRepository
                .findByUserIdAndMetricDateBetween(userId, periodStart, today);

        BigDecimal totalSpent = metrics.stream()
                .map(UserMetrics::getTotalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Mock category breakdown
        List<CategoryBreakdown> breakdown = List.of(
                CategoryBreakdown.builder()
                        .category("Food & Dining")
                        .amount(totalSpent.multiply(new BigDecimal("0.30")))
                        .count(15)
                        .percentage(30.0)
                        .build(),
                CategoryBreakdown.builder()
                        .category("Transportation")
                        .amount(totalSpent.multiply(new BigDecimal("0.25")))
                        .count(10)
                        .percentage(25.0)
                        .build(),
                CategoryBreakdown.builder()
                        .category("Shopping")
                        .amount(totalSpent.multiply(new BigDecimal("0.20")))
                        .count(8)
                        .percentage(20.0)
                        .build()
        );

        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/trends/balance")
    @PreAuthorize("#userId == authentication.principal.claims['sub'] or hasRole('ADMIN')")
    public ResponseEntity<BalancedTrend.BalanceTrend> getBalanceTrend(
            @RequestParam String userId,
            @RequestParam(defaultValue = "30") int days) {

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<UserMetrics> metrics = userMetricsRepository
                .findByUserIdAndMetricDateBetween(userId, startDate, endDate);

        List<DataPoint> dataPoints = metrics.stream()
                .map(m -> DataPoint.builder()
                        .timestamp(m.getMetricDate().atStartOfDay())
                        .value(m.getAccountBalance())
                        .build())
                .collect(Collectors.toList());

        BalancedTrend.BalanceTrend trend = BalancedTrend.BalanceTrend.builder()
                .period(days + " days")
                .dataPoints(dataPoints)
                .build();

        return ResponseEntity.ok(trend);
    }

    @GetMapping("/insights/recommendations")
    @PreAuthorize("#userId == authentication.principal.claims['sub'] or hasRole('ADMIN')")
    public ResponseEntity<List<String>> getRecommendations(@RequestParam String userId) {
        // Simplified recommendations
        List<String> recommendations = List.of(
                "Your spending is 15% higher than last month. Consider reviewing your budget.",
                "You have 3 recurring subscriptions. Cancel unused ones to save money.",
                "Based on your savings pattern, you could save an extra 200 MAD per month."
        );

        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/admin/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOverview> getAdminOverview() {
        LocalDate today = LocalDate.now();

        long activeUsers = userMetricsRepository.countDistinctUserIds(today);

        AdminOverview overview = AdminOverview.builder()
                .activeUsers(activeUsers)
                .totalTransactions(15234L)
                .revenue(new BigDecimal("125000"))
                .build();

        return ResponseEntity.ok(overview);
    }
}
