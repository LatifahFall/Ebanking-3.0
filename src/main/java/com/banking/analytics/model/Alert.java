// ========== Alert.java ==========
package com.banking.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_user_status", columnList = "user_id,status"),
        @Index(name = "idx_alert_type", columnList = "alert_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String alertId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "alert_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(name = "severity", nullable = false)
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "threshold_value", precision = 15, scale = 2)
    private BigDecimal thresholdValue;

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "notified")
    private boolean notified;

    public enum AlertType {
        SPENDING_THRESHOLD,
        LOW_BALANCE,
        UNUSUAL_ACTIVITY,
        BUDGET_EXCEEDED,
        LARGE_TRANSACTION,
        FREQUENT_TRANSACTIONS,
        SUSPICIOUS_LOGIN
    }

    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }

    public enum AlertStatus {
        ACTIVE,
        RESOLVED,
        IGNORED
    }

    @PrePersist
    protected void onCreate() {
        if (triggeredAt == null) {
            triggeredAt = LocalDateTime.now();
        }
        if (status == null) {
            status = AlertStatus.ACTIVE;
        }
    }
}