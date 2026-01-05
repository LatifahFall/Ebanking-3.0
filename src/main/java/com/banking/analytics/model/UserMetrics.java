package com.banking.analytics.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_metrics", indexes = {
        @Index(name = "idx_user_date", columnList = "user_id,metric_date"),
        @Index(name = "idx_metric_date", columnList = "metric_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "total_transactions")
    private Integer totalTransactions;

    @Column(name = "total_spent", precision = 15, scale = 2)
    private BigDecimal totalSpent;

    @Column(name = "total_received", precision = 15, scale = 2)
    private BigDecimal totalReceived;

    @Column(name = "account_balance", precision = 15, scale = 2)
    private BigDecimal accountBalance;

    @Column(name = "crypto_value", precision = 15, scale = 2)
    private BigDecimal cryptoValue;

    @Column(name = "login_count")
    private Integer loginCount;

    @Column(name = "failed_transactions")
    private Integer failedTransactions;

    @Column(name = "notifications_sent")
    private Integer notificationsSent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}







