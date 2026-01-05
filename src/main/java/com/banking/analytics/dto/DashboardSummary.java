// ========== DTOs ==========
package com.banking.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private String userId;
    private BigDecimal currentBalance;
    private BigDecimal monthlySpending;
    private BigDecimal monthlyIncome;
    private Integer transactionsThisMonth;
    private List<CategoryBreakdown> topCategories;
    private BalancedTrend.BalanceTrend balanceTrend;
    private List<RecentTransaction> recentTransactions;
    private LocalDateTime generatedAt;
}





