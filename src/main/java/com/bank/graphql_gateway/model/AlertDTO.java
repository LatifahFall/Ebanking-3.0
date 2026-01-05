package com.bank.graphql_gateway.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlertDTO {
    private String alertId;
    private String userId;
    private String alertType;
    private String severity;
    private String title;
    private String message;
    private BigDecimal thresholdValue;
    private BigDecimal currentValue;
    private String status;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;
    private boolean notified;

    // Constructors
    public AlertDTO() {}

    public AlertDTO(String alertId, String userId, String alertType, String severity, 
                   String title, String message, BigDecimal thresholdValue, 
                   BigDecimal currentValue, String status, LocalDateTime triggeredAt, 
                   LocalDateTime resolvedAt, boolean notified) {
        this.alertId = alertId;
        this.userId = userId;
        this.alertType = alertType;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.thresholdValue = thresholdValue;
        this.currentValue = currentValue;
        this.status = status;
        this.triggeredAt = triggeredAt;
        this.resolvedAt = resolvedAt;
        this.notified = notified;
    }

    // Getters and Setters
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public BigDecimal getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }

    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
}
