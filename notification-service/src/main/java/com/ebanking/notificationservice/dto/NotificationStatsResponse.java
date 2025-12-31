package com.ebanking.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour statistiques de notifications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsResponse {
    private long totalSent;
    private long totalFailed;
    private long emailsSent;
    private long smsSent;
    private long pushSent;
    private double successRate;
}