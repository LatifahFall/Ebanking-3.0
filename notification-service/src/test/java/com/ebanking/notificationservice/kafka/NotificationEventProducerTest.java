package com.ebanking.notificationservice.kafka;

import com.ebanking.notificationservice.dto.NotificationEvent;
import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.model.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour NotificationEventProducer
 */
@ExtendWith(MockitoExtension.class)
class NotificationEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationEventProducer eventProducer;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    @Captor
    private ArgumentCaptor<NotificationEvent> eventCaptor;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(123L);
        testNotification.setUserId("user123");
        testNotification.setType(NotificationType.EMAIL);
        testNotification.setRecipient("test@example.com");
        testNotification.setSubject("Test Subject");
        testNotification.setMessage("Test Message");
        testNotification.setSentAt(LocalDateTime.now());
        testNotification.setStatus("SENT");
    }

    @Test
    void testPublishNotificationSent() {
        // Given
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When
        eventProducer.publishNotificationSent(testNotification);

        // Then
        verify(kafkaTemplate).send(
                topicCaptor.capture(),
                keyCaptor.capture(),
                eventCaptor.capture()
        );

        assertEquals("notification-status", topicCaptor.getValue());
        assertEquals("user123", keyCaptor.getValue());

        NotificationEvent event = eventCaptor.getValue();
        assertNotNull(event);
        assertEquals(NotificationEvent.EventType.NOTIFICATION_SENT, event.getEventType());
        assertEquals("notification-service", event.getSourceService());
        assertNotNull(event.getResult());
        assertEquals(123L, event.getResult().getNotificationId());
        assertEquals("SENT", event.getResult().getStatus());
    }

    @Test
    void testPublishNotificationFailed() {
        // Given
        String errorMessage = "SMTP connection failed";
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When
        eventProducer.publishNotificationFailed(testNotification, errorMessage);

        // Then
        verify(kafkaTemplate).send(
                topicCaptor.capture(),
                keyCaptor.capture(),
                eventCaptor.capture()
        );

        assertEquals("notification-status", topicCaptor.getValue());
        assertEquals("user123", keyCaptor.getValue());

        NotificationEvent event = eventCaptor.getValue();
        assertNotNull(event);
        assertEquals(NotificationEvent.EventType.NOTIFICATION_FAILED, event.getEventType());
        assertNotNull(event.getResult());
        assertEquals("FAILED", event.getResult().getStatus());
        assertEquals(errorMessage, event.getResult().getErrorMessage());
    }

    @Test
    void testPublishNotificationDelivered() {
        // Given
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When
        eventProducer.publishNotificationDelivered(testNotification);

        // Then
        verify(kafkaTemplate).send(
                topicCaptor.capture(),
                keyCaptor.capture(),
                eventCaptor.capture()
        );

        NotificationEvent event = eventCaptor.getValue();
        assertEquals(NotificationEvent.EventType.NOTIFICATION_DELIVERED, event.getEventType());
        assertEquals("DELIVERED", event.getResult().getStatus());
    }

    @Test
    void testPublishNotificationRead() {
        // Given
        Long notificationId = 456L;
        String userId = "user789";
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When
        eventProducer.publishNotificationRead(notificationId, userId);

        // Then
        verify(kafkaTemplate).send(
                topicCaptor.capture(),
                keyCaptor.capture(),
                eventCaptor.capture()
        );

        assertEquals("notification-status", topicCaptor.getValue());
        assertEquals(userId, keyCaptor.getValue());

        NotificationEvent event = eventCaptor.getValue();
        assertEquals(NotificationEvent.EventType.NOTIFICATION_READ, event.getEventType());
        assertEquals(notificationId, event.getResult().getNotificationId());
        assertEquals("READ", event.getResult().getStatus());
    }

    @Test
    void testPublishAuditEvent() {
        // Given
        String userId = "user123";
        String action = "NOTIFICATION_SENT";
        String details = "Email sent successfully";
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When
        eventProducer.publishAuditEvent(userId, action, details);

        // Then
        verify(kafkaTemplate).send(
                eq("notification-audit"),
                eq(userId),
                any(NotificationEvent.class)
        );
    }

    @Test
    void testSendEventHandlesException() {
        // Given
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> 
            eventProducer.publishNotificationSent(testNotification)
        );
    }

    @Test
    void testEventHasCorrectTimestamp() {
        // Given
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);
        LocalDateTime before = LocalDateTime.now();

        // When
        eventProducer.publishNotificationSent(testNotification);

        // Then
        verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());
        NotificationEvent event = eventCaptor.getValue();
        
        LocalDateTime after = LocalDateTime.now();
        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp().isAfter(before.minusSeconds(1)));
        assertTrue(event.getTimestamp().isBefore(after.plusSeconds(1)));
    }

    @Test
    void testEventIdIsUnique() {
        // Given
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When
        eventProducer.publishNotificationSent(testNotification);
        eventProducer.publishNotificationSent(testNotification);

        // Then
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), eventCaptor.capture());
        
        String eventId1 = eventCaptor.getAllValues().get(0).getEventId();
        String eventId2 = eventCaptor.getAllValues().get(1).getEventId();
        
        assertNotNull(eventId1);
        assertNotNull(eventId2);
        assertNotEquals(eventId1, eventId2);
    }
}
