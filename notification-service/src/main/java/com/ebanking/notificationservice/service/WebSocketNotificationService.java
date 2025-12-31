package com.ebanking.notificationservice.service;

import com.ebanking.notificationservice.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendPushToUser(String userId, Notification notification) {
        try {
            // Envoyer au topic user-specific
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/notifications",
                    notification
            );
            log.info("🔔 Push notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("❌ Failed to send WebSocket notification: {}", e.getMessage());
        }
    }
}