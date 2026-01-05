package com.bank.graphql_gateway.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardSummaryDTO {
    private String userId;
    private BigDecimal currentBalance;
    private BigDecimal monthlySpending;
    private BigDecimal monthlyIncome;
    private Integer transactionsThisMonth;
    private List<CategoryBreakdownDTO> topCategories;
    private BalanceTrendDTO balanceTrend;
    private List<RecentTransactionDTO> recentTransactions;
    private LocalDateTime generatedAt;

    // Constructors
    public DashboardSummaryDTO() {}

    public DashboardSummaryDTO(String userId, BigDecimal currentBalance, 
                              BigDecimal monthlySpending, BigDecimal monthlyIncome, 
                              Integer transactionsThisMonth, List<CategoryBreakdownDTO> topCategories,
                              BalanceTrendDTO balanceTrend, List<RecentTransactionDTO> recentTransactions,
                              LocalDateTime generatedAt) {
        this.userId = userId;
        this.currentBalance = currentBalance;
        this.monthlySpending = monthlySpending;
        this.monthlyIncome = monthlyIncome;
        this.transactionsThisMonth = transactionsThisMonth;
        this.topCategories = topCategories;
        this.balanceTrend = balanceTrend;
        this.recentTransactions = recentTransactions;
        this.generatedAt = generatedAt;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public BigDecimal getMonthlySpending() { return monthlySpending; }
    public void setMonthlySpending(BigDecimal monthlySpending) { this.monthlySpending = monthlySpending; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public Integer getTransactionsThisMonth() { return transactionsThisMonth; }
    public void setTransactionsThisMonth(Integer transactionsThisMonth) { 
        this.transactionsThisMonth = transactionsThisMonth; 
    }

    public List<CategoryBreakdownDTO> getTopCategories() { return topCategories; }
    public void setTopCategories(List<CategoryBreakdownDTO> topCategories) { 
        this.topCategories = topCategories; 
    }

    public BalanceTrendDTO getBalanceTrend() { return balanceTrend; }
    public void setBalanceTrend(BalanceTrendDTO balanceTrend) { this.balanceTrend = balanceTrend; }

    public List<RecentTransactionDTO> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<RecentTransactionDTO> recentTransactions) { 
        this.recentTransactions = recentTransactions; 
    }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
