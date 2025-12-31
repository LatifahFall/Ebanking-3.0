package com.ebanking.notificationservice.kafka;

import com.ebanking.notificationservice.dto.NotificationEvent;
import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.model.NotificationType;
import com.ebanking.notificationservice.service.NotificationService;
import com.ebanking.notificationservice.service.NotificationTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour NotificationEventConsumer
 */
@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationTemplateService templateService;

    @Mock
    private Counter kafkaEventsCounter;

    private ObjectMapper objectMapper;
    private NotificationEventConsumer eventConsumer;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private String transactionEventJson;
    private String paymentEventJson;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        eventConsumer = new NotificationEventConsumer(notificationService, templateService, objectMapper, kafkaEventsCounter);
        
        transactionEventJson = """
            {
              "userId": "user123",
              "userEmail": "test@example.com",
              "userName": "John Doe",
              "transactionId": "txn_abc123",
              "amount": 150.0,
              "date": "2024-12-16T10:30:00"
            }
            """;

        paymentEventJson = """
            {
              "userId": "user456",
              "userEmail": "payment@example.com",
              "userPhone": "+33612345678",
              "paymentId": "pay_xyz789",
              "amount": 250.0,
              "beneficiary": "COMPANY XYZ",
              "status": "COMPLETED",
              "date": "2024-12-16T11:00:00"
            }
            """;
    }

    @Test
    void testHandleTransactionCompletedSuccess() throws Exception {
        // Given
        when(templateService.generateTransactionEmailTemplate(anyMap()))
                .thenReturn("<html>Transaction completed</html>");
        when(notificationService.sendNotification(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventConsumer.handleTransactionCompleted(
                transactionEventJson,
                "user123",
                0,
                100L
        );

        // Then
        verify(kafkaEventsCounter).increment();
        verify(notificationService).sendNotification(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();
        assertEquals("user123", notification.getUserId());
        assertEquals(NotificationType.EMAIL, notification.getType());
        assertEquals("test@example.com", notification.getRecipient());
        assertTrue(notification.getSubject().contains("txn_abc123"));
    }

    @Test
    void testHandlePaymentCompletedSuccess() throws Exception {
        // Given
        when(templateService.generatePaymentEmailTemplate(anyMap()))
                .thenReturn("<html>Payment completed</html>");
        when(notificationService.sendNotification(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventConsumer.handlePaymentCompleted(
                paymentEventJson,
                "user456"
        );

        // Then
        verify(kafkaEventsCounter).increment();
        verify(notificationService, atLeastOnce()).sendNotification(any(Notification.class));
    }

    @Test
    void testHandleTransactionCompletedWithMissingFields() throws Exception {
        // Given
        String incompleteJson = """
            {
              "userId": "user789",
              "userEmail": "incomplete@example.com"
            }
            """;

        // When/Then - should handle gracefully without throwing
        assertDoesNotThrow(() ->
                eventConsumer.handleTransactionCompleted(
                        incompleteJson,
                        "user789",
                        0,
                        300L
                )
        );
    }

    @Test
    void testKafkaMetricsIncremented() throws Exception {
        // Given
        when(templateService.generateTransactionEmailTemplate(anyMap()))
                .thenReturn("<html>Test</html>");
        when(notificationService.sendNotification(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventConsumer.handleTransactionCompleted(
                transactionEventJson,
                "user123",
                0,
                100L
        );

        // Then
        verify(kafkaEventsCounter).increment();
    }

    @Test
    void testHandleInvalidJson() {
        // Given
        String invalidJson = "{ invalid json }";

        // When/Then - should log error but not throw
        assertDoesNotThrow(() ->
                eventConsumer.handleTransactionCompleted(
                        invalidJson,
                        "user999",
                        0,
                        500L
                )
        );

        // Verify notification was not sent
        verify(notificationService, never()).sendNotification(any());
    }

    @Test
    void testHandleAccountCreatedSuccess() {
        // Given
        String accountCreatedJson = """
            {
              "userId": "user001",
              "userEmail": "newuser@example.com",
              "userName": "Alice Dupont",
              "accountType": "SAVINGS",
              "createdAt": "2024-12-16T10:30:00"
            }
            """;

        when(templateService.generateWelcomeEmailTemplate(anyMap()))
                .thenReturn("<html>Welcome email</html>");
        when(notificationService.sendNotification(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> eventConsumer.handleAccountCreated(accountCreatedJson));

        // Then
        verify(kafkaEventsCounter).increment();
        verify(notificationService, times(2)).sendNotification(notificationCaptor.capture());

        // Verify EMAIL notification
        Notification emailNotif = notificationCaptor.getAllValues().get(0);
        assertEquals("user001", emailNotif.getUserId());
        assertEquals(NotificationType.EMAIL, emailNotif.getType());
        assertEquals("newuser@example.com", emailNotif.getRecipient());
        assertTrue(emailNotif.getSubject().contains("Bienvenue"));

        // Verify IN_APP notification
        Notification inAppNotif = notificationCaptor.getAllValues().get(1);
        assertEquals(NotificationType.IN_APP, inAppNotif.getType());
    }

    @Test
    void testHandleKycStatusChangedApproved() {
        // Given
        String kycApprovedJson = """
            {
              "userId": "user002",
              "userEmail": "user002@example.com",
              "userName": "Bob Martin",
              "previousStatus": "PENDING",
              "newStatus": "APPROVED",
              "reason": "Documents validés",
              "timestamp": "2024-12-16T10:30:00"
            }
            """;

        when(templateService.generateKycStatusEmailTemplate(anyMap()))
                .thenReturn("<html>KYC Approved</html>");
        when(notificationService.sendNotification(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> eventConsumer.handleKycStatusChanged(kycApprovedJson));

        // Then
        verify(kafkaEventsCounter).increment();
        verify(notificationService, times(2)).sendNotification(notificationCaptor.capture());

        // Verify EMAIL + PUSH sent for APPROVED status
        assertEquals(2, notificationCaptor.getAllValues().size());
        assertTrue(notificationCaptor.getAllValues().stream()
                .anyMatch(n -> n.getType() == NotificationType.EMAIL));
        assertTrue(notificationCaptor.getAllValues().stream()
                .anyMatch(n -> n.getType() == NotificationType.PUSH));
    }

    @Test
    void testHandleKycStatusChangedPending() {
        // Given
        String kycPendingJson = """
            {
              "userId": "user003",
              "userEmail": "user003@example.com",
              "userName": "Charlie Lee",
              "previousStatus": "SUBMITTED",
              "newStatus": "PENDING",
              "timestamp": "2024-12-16T10:30:00"
            }
            """;

        when(templateService.generateKycStatusEmailTemplate(anyMap()))
                .thenReturn("<html>KYC Pending</html>");
        when(notificationService.sendNotification(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> eventConsumer.handleKycStatusChanged(kycPendingJson));

        // Then
        verify(kafkaEventsCounter).increment();
        // Only EMAIL sent for PENDING (no PUSH)
        verify(notificationService, times(1)).sendNotification(notificationCaptor.capture());
        assertEquals(NotificationType.EMAIL, notificationCaptor.getValue().getType());
    }

    @Test
    void testHandleCryptoTransactionBuy() {
        // Given
        String cryptoBuyJson = """
            {
              "userId": "user004",
              "userEmail": "crypto@example.com",
              "userName": "Diana Prince",
              "transactionType": "BUY",
              "cryptocurrency": "BTC",
              "amount": 0.05,
              "fiatAmount": 1500.0,
              "fiatCurrency": "EUR",
              "rate": 30000.0,
              "timestamp": "2024-12-16T10:30:00"
            }
            """;

        when(templateService.generateCryptoTransactionEmailTemplate(anyMap()))
                .thenReturn("<html>Crypto Buy</html>");
        when(notificationService.sendNotification(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> eventConsumer.handleCryptoTransaction(cryptoBuyJson));

        // Then
        verify(kafkaEventsCounter).increment();
        verify(notificationService, times(2)).sendNotification(notificationCaptor.capture());

        // Verify EMAIL + PUSH
        assertTrue(notificationCaptor.getAllValues().stream()
                .anyMatch(n -> n.getSubject().contains("Achat")));
        assertTrue(notificationCaptor.getAllValues().stream()
                .anyMatch(n -> n.getMessage().contains("BTC")));
    }

    @Test
    void testHandleCryptoTransactionSell() {
        // Given
        String cryptoSellJson = """
            {
              "userId": "user005",
              "userEmail": "trader@example.com",
              "userName": "Eve Trader",
              "transactionType": "SELL",
              "cryptocurrency": "ETH",
              "amount": 1.5,
              "fiatAmount": 2000.0,
              "fiatCurrency": "USD",
              "timestamp": "2024-12-16T10:30:00"
            }
            """;

        when(templateService.generateCryptoTransactionEmailTemplate(anyMap()))
                .thenReturn("<html>Crypto Sell</html>");
        when(notificationService.sendNotification(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> eventConsumer.handleCryptoTransaction(cryptoSellJson));

        // Then
        verify(kafkaEventsCounter).increment();
        verify(notificationService, times(2)).sendNotification(notificationCaptor.capture());

        // Verify "Vente" in subject
        assertTrue(notificationCaptor.getAllValues().stream()
                .anyMatch(n -> n.getSubject().contains("Vente")));
    }

    @Test
    void testHandleNotificationRequestedEmail() {
        // Given
        String notificationRequestJson = """
            {
              "eventType": "NOTIFICATION_REQUESTED",
              "eventId": "evt_123",
              "timestamp": "2024-12-16T10:30:00",
              "sourceService": "payment-service",
              "notificationData": {
                "userId": "user006",
                "recipient": "generic@example.com",
                "type": "EMAIL",
                "subject": "Custom Subject",
                "message": "Custom message content",
                "priority": "HIGH"
              }
            }
            """;

        when(notificationService.sendNotification(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> eventConsumer.handleNotificationRequested(notificationRequestJson));

        // Then
        verify(kafkaEventsCounter).increment();
        verify(notificationService).sendNotification(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();
        assertEquals("user006", notification.getUserId());
        assertEquals(NotificationType.EMAIL, notification.getType());
        assertEquals("generic@example.com", notification.getRecipient());
        assertEquals("Custom Subject", notification.getSubject());
        assertEquals("Custom message content", notification.getMessage());
    }

    @Test
    void testHandleNotificationRequestedSms() {
        // Given
        String smsRequestJson = """
            {
              "eventType": "NOTIFICATION_REQUESTED",
              "eventId": "evt_456",
              "timestamp": "2024-12-16T10:30:00",
              "sourceService": "auth-service",
              "notificationData": {
                "userId": "user007",
                "recipient": "+33612345678",
                "type": "SMS",
                "subject": "Verification Code",
                "message": "Your code is: 123456",
                "priority": "URGENT"
              }
            }
            """;

        when(notificationService.sendNotification(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> eventConsumer.handleNotificationRequested(smsRequestJson));

        // Then
        verify(kafkaEventsCounter).increment();
        verify(notificationService).sendNotification(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();
        assertEquals(NotificationType.SMS, notification.getType());
        assertEquals("+33612345678", notification.getRecipient());
        assertTrue(notification.getMessage().contains("123456"));
    }
}

