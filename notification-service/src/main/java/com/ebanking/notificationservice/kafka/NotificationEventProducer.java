package com.ebanking.notificationservice.kafka;

import com.ebanking.notificationservice.dto.NotificationEvent;
import com.ebanking.notificationservice.model.Notification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Producer Kafka pour publier les événements de notification
 * Publie sur différents topics selon le type d'événement
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // Topics Kafka
    private static final String TOPIC_NOTIFICATION_STATUS = "notification-status";
    private static final String TOPIC_NOTIFICATION_AUDIT = "notification-audit";
    private static final String TOPIC_NOTIFICATION_METRICS = "notification-metrics";

    /**
     * Publier un événement de notification envoyée avec succès
     */
    public void publishNotificationSent(Notification notification) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(NotificationEvent.EventType.NOTIFICATION_SENT)
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .sourceService("notification-service")
                .result(NotificationEvent.NotificationResult.builder()
                        .notificationId(notification.getId())
                        .status("SENT")
                        .sentAt(notification.getSentAt())
                        .provider(notification.getType().name())
                        .build())
                .build();

        sendEvent(TOPIC_NOTIFICATION_STATUS, notification.getUserId(), event);
        log.info("📤 Published NOTIFICATION_SENT event for notification ID: {}", notification.getId());
    }

    /**
     * Publier un événement d'échec d'envoi
     */
    public void publishNotificationFailed(Notification notification, String errorMessage) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(NotificationEvent.EventType.NOTIFICATION_FAILED)
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .sourceService("notification-service")
                .result(NotificationEvent.NotificationResult.builder()
                        .notificationId(notification.getId())
                        .status("FAILED")
                        .errorMessage(errorMessage)
                        .provider(notification.getType().name())
                        .build())
                .build();

        sendEvent(TOPIC_NOTIFICATION_STATUS, notification.getUserId(), event);
        log.error("📤 Published NOTIFICATION_FAILED event for notification ID: {}", notification.getId());
    }

    /**
     * Publier un événement de notification livrée
     */
    public void publishNotificationDelivered(Notification notification) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(NotificationEvent.EventType.NOTIFICATION_DELIVERED)
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .sourceService("notification-service")
                .result(NotificationEvent.NotificationResult.builder()
                        .notificationId(notification.getId())
                        .status("DELIVERED")
                        .sentAt(notification.getSentAt())
                        .provider(notification.getType().name())
                        .build())
                .build();

        sendEvent(TOPIC_NOTIFICATION_STATUS, notification.getUserId(), event);
        log.info("📤 Published NOTIFICATION_DELIVERED event for notification ID: {}", notification.getId());
    }

    /**
     * Publier un événement de notification lue
     */
    public void publishNotificationRead(Long notificationId, String userId) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(NotificationEvent.EventType.NOTIFICATION_READ)
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .sourceService("notification-service")
                .result(NotificationEvent.NotificationResult.builder()
                        .notificationId(notificationId)
                        .status("READ")
                        .build())
                .build();

        sendEvent(TOPIC_NOTIFICATION_STATUS, userId, event);
        log.info("📤 Published NOTIFICATION_READ event for notification ID: {}", notificationId);
    }

    /**
     * Publier un événement d'audit
     */
    public void publishAuditEvent(String userId, String action, String details) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(NotificationEvent.EventType.NOTIFICATION_SENT)
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .sourceService("notification-service")
                .notificationData(NotificationEvent.NotificationData.builder()
                        .userId(userId)
                        .message(details)
                        .build())
                .build();

        sendEvent(TOPIC_NOTIFICATION_AUDIT, userId, event);
        log.debug("📋 Published audit event for user: {}", userId);
    }

    /**
     * Envoyer un événement sur un topic Kafka
     */
    private void sendEvent(String topic, String key, NotificationEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("✅ Event sent successfully to topic [{}] with offset [{}]", 
                            topic, result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Failed to send event to topic [{}]: {}", topic, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("❌ Error sending event to Kafka topic [{}]: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * Envoyer un événement générique (pour debug/monitoring)
     */
    public void sendGenericEvent(String topic, String key, Object payload) {
        try {
            String message = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, message);
            log.info("📤 Generic event sent to topic [{}]", topic);
        } catch (JsonProcessingException e) {
            log.error("❌ Error serializing event: {}", e.getMessage());
        }
    }
}
