package com.ebanking.notificationservice.service;

import com.ebanking.notificationservice.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final WebSocketNotificationService webSocketService;

    public void sendPushNotification(Notification notification) {
        try {
            // Envoyer via WebSocket
            webSocketService.sendPushToUser(
                    notification.getUserId(),
                    notification
            );
            log.info("✅ Push notification sent successfully");
        } catch (Exception e) {
            log.error("❌ Failed to send push: {}", e.getMessage());
        }
    }
}