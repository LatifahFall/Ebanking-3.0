package com.bank.graphql_gateway.model;

import java.math.BigDecimal;

public class AdminOverviewDTO {
    private Long activeUsers;
    private Long totalTransactions;
    private BigDecimal revenue;

    // Constructors
    public AdminOverviewDTO() {}

    public AdminOverviewDTO(Long activeUsers, Long totalTransactions, BigDecimal revenue) {
        this.activeUsers = activeUsers;
        this.totalTransactions = totalTransactions;
        this.revenue = revenue;
    }

    // Getters and Setters
    public Long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }

    public Long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Long totalTransactions) { 
        this.totalTransactions = totalTransactions; 
    }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
}
