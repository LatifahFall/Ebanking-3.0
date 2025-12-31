package com.ebanking.notificationservice;

import com.ebanking.notificationservice.kafka.NotificationEventProducer;
import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.model.NotificationType;
import com.ebanking.notificationservice.repository.NotificationRepository;
import com.ebanking.notificationservice.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour NotificationService - Version Corrigée avec Kafka Producer
 *
 * CORRECTIONS :
 * - Ajout du mock NotificationEventProducer
 * - Utilisation de lenient() pour les stubs non utilisés
 * - Mock configuré uniquement quand nécessaire
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private NotificationTemplateService templateService;

    @Mock
    private NotificationPreferenceService preferenceService;

    @Mock
    private NotificationAuditService auditService;

    @Mock
    private NotificationEventProducer eventProducer;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setUserId("user123");
        testNotification.setType(NotificationType.EMAIL);
        testNotification.setRecipient("test@example.com");
        testNotification.setSubject("Test Subject");
        testNotification.setMessage("Test Message");

        // CORRECTION : Utiliser lenient() pour éviter UnnecessaryStubbingException
        // Ce stub est utilisé par CERTAINS tests mais pas tous
        lenient().when(preferenceService.shouldSendNotification(anyString(), any(), anyString()))
                .thenReturn(true);
    }

    @Test
    void sendNotification_Email_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification);
        doNothing().when(emailService).sendEmail(any(Notification.class));
        doNothing().when(auditService).logNotificationSent(any(Notification.class));

        // Act
        Notification result = notificationService.sendNotification(testNotification);

        // Assert
        assertNotNull(result);
        assertEquals("SENT", result.getStatus());
        assertNotNull(result.getSentAt());

        verify(emailService, times(1)).sendEmail(any(Notification.class));
        verify(preferenceService, times(1)).shouldSendNotification(anyString(), any(), anyString());
        verify(auditService, times(1)).logNotificationSent(any(Notification.class));
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendNotification_SMS_Success() {
        // Arrange
        testNotification.setType(NotificationType.SMS);
        testNotification.setRecipient("+212600000000");

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification);
        doNothing().when(smsService).sendSms(any(Notification.class));
        doNothing().when(auditService).logNotificationSent(any(Notification.class));

        // Act
        Notification result = notificationService.sendNotification(testNotification);

        // Assert
        assertEquals("SENT", result.getStatus());
        verify(smsService, times(1)).sendSms(any(Notification.class));
        verify(auditService, times(1)).logNotificationSent(any(Notification.class));
    }

    @Test
    void sendNotification_Push_Success() {
        // Arrange
        testNotification.setType(NotificationType.PUSH);

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification);
        doNothing().when(pushNotificationService).sendPushNotification(any(Notification.class));
        doNothing().when(auditService).logNotificationSent(any(Notification.class));

        // Act
        Notification result = notificationService.sendNotification(testNotification);

        // Assert
        assertEquals("SENT", result.getStatus());
        verify(pushNotificationService, times(1)).sendPushNotification(any(Notification.class));
        verify(auditService, times(1)).logNotificationSent(any(Notification.class));
    }

    @Test
    void sendNotification_InApp_Success() {
        // Arrange
        testNotification.setType(NotificationType.IN_APP);

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification);
        doNothing().when(auditService).logNotificationSent(any(Notification.class));

        // Act
        Notification result = notificationService.sendNotification(testNotification);

        // Assert
        assertEquals("SENT", result.getStatus());
        verify(emailService, never()).sendEmail(any());
        verify(smsService, never()).sendSms(any());
        verify(pushNotificationService, never()).sendPushNotification(any());
        verify(auditService, times(1)).logNotificationSent(any(Notification.class));
    }

    @Test
    void sendNotification_Skipped_WhenUserPreferencesDisabled() {
        // Arrange
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification);

        // Override le stub lenient pour ce test
        when(preferenceService.shouldSendNotification(anyString(), any(), anyString()))
                .thenReturn(false);

        doNothing().when(auditService).logNotificationSent(any(Notification.class));

        // Act
        Notification result = notificationService.sendNotification(testNotification);

        // Assert
        assertEquals("SKIPPED", result.getStatus());

        verify(emailService, never()).sendEmail(any());
        verify(smsService, never()).sendSms(any());
        verify(pushNotificationService, never()).sendPushNotification(any());
        verify(auditService, times(1)).logNotificationSent(any(Notification.class));
    }

    @Test
    void sendNotification_EmailFails_StatusFailed() {
        // Arrange
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification);

        doThrow(new RuntimeException("Email service down"))
                .when(emailService).sendEmail(any(Notification.class));

        doNothing().when(auditService).logNotificationFailure(any(Notification.class), anyString());

        // Act
        Notification result = notificationService.sendNotification(testNotification);

        // Assert
        assertEquals("FAILED", result.getStatus());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Email service down"));
        verify(auditService, times(1)).logNotificationFailure(any(Notification.class), anyString());
    }

    @Test
    void getUserNotifications_ReturnsUserNotifications() {
        // Arrange
        String userId = "user123";
        when(notificationRepository.findByUserId(userId))
                .thenReturn(java.util.List.of(testNotification));

        // Act
        var result = notificationService.getUserNotifications(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getInAppNotifications_ReturnsInAppOnly() {
        // Arrange
        String userId = "user123";
        testNotification.setType(NotificationType.IN_APP);

        when(notificationRepository.findByUserIdAndType(userId, NotificationType.IN_APP))
                .thenReturn(java.util.List.of(testNotification));

        // Act
        var result = notificationService.getInAppNotifications(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(NotificationType.IN_APP, result.get(0).getType());
        verify(notificationRepository, times(1)).findByUserIdAndType(userId, NotificationType.IN_APP);
    }

    @Test
    void getPendingNotifications_ReturnsPendingOnly() {
        // Arrange
        testNotification.setStatus("PENDING");

        when(notificationRepository.findByStatus("PENDING"))
                .thenReturn(java.util.List.of(testNotification));

        // Act
        var result = notificationService.getPendingNotifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
        verify(notificationRepository, times(1)).findByStatus("PENDING");
    }

    @Test
    void markAsRead_UpdatesNotificationStatus() {
        // Arrange
        Long notificationId = 1L;
        testNotification.setId(notificationId);
        testNotification.setType(NotificationType.IN_APP);

        when(notificationRepository.findById(notificationId))
                .thenReturn(java.util.Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification);

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void markAsRead_ThrowsException_WhenNotificationNotFound() {
        // Arrange
        Long notificationId = 999L;
        when(notificationRepository.findById(notificationId))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            notificationService.markAsRead(notificationId);
        });
    }
}