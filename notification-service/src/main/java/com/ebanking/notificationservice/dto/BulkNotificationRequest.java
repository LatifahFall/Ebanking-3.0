package com.ebanking.notificationservice.dto;

import com.ebanking.notificationservice.model.NotificationType;
import lombok.Data;

import java.util.List;

/**
 * DTO pour envoi de notifications en masse
 */
@Data
public class BulkNotificationRequest {
    private List<String> userIds;
    private NotificationType type;
    private String subject;
    private String message;
    private String category; // transaction, payment, security, marketing
}

