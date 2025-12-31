package com.ebanking.notificationservice.dto;

import lombok.Data;

/**
 * DTO pour mise à jour des préférences
 */
@Data
public class NotificationPreferenceRequest {
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean inAppEnabled;

    private boolean transactionNotifications;
    private boolean paymentNotifications;
    private boolean securityAlerts;
    private boolean marketingNotifications;

    private boolean doNotDisturb;
    private String doNotDisturbStart;
    private String doNotDisturbEnd;
}