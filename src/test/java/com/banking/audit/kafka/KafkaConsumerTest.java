package com.banking.audit.kafka;

import com.banking.audit.BaseIntegrationTest;
import com.banking.audit.model.AuditEvent;
import com.banking.audit.repository.AuditEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

class KafkaConsumerTest extends BaseIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AuditEventRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> BaseIntegrationTest.kafka.getBootstrapServers());
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    // ==========================================
    // AUTH SERVICE EVENTS TESTS
    // ==========================================
    @Nested
    @DisplayName("Auth Service Events")
    class AuthServiceTests {

        @Test
        @DisplayName("Should consume and save LOGIN_SUCCESS event")
        void shouldConsumeLoginSuccessEvent() throws Exception {
            // Given
            Map<String, Object> authEvent = new HashMap<>();
            authEvent.put("eventType", "LOGIN_SUCCESS");
            authEvent.put("userId", 123L);
            authEvent.put("sessionId", "session_abc");
            authEvent.put("ip", "192.168.0.2");
            authEvent.put("device", "Chrome/Windows");
            authEvent.put("location", "Casablanca, Morocco");

            String message = objectMapper.writeValueAsString(authEvent);

            // When
            kafkaTemplate.send("auth.events", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);

                AuditEvent event = events.get(0);
                assertThat(event.getEventType()).isEqualTo(AuditEvent.EventType.USER_LOGIN);
                assertThat(event.getUserId()).isEqualTo(123L);
                assertThat(event.getSessionId()).isEqualTo("session_abc");
                assertThat(event.getServiceSource()).isEqualTo("auth-service");
                assertThat(event.getIpAddress()).isEqualTo("192.168.0.2");
                assertThat(event.getUserAgent()).isEqualTo("Chrome/Windows");
                assertThat(event.getGeolocation()).isEqualTo("Casablanca, Morocco");
                assertThat(event.getResult()).isEqualTo(AuditEvent.AuditResult.SUCCESS);
                assertThat(event.getComplianceFlags()).containsExactlyInAnyOrder("SECURITY", "AUTHENTICATION");
                assertThat(event.getRiskScore()).isEqualTo(0.1);
                assertThat(event.isSensitive()).isTrue();
            });
        }

        @Test
        @DisplayName("Should consume and save LOGIN_FAILED event with high risk score")
        void shouldConsumeLoginFailedEvent() throws Exception {
            // Given
            Map<String, Object> authEvent = new HashMap<>();
            authEvent.put("eventType", "LOGIN_FAILED");
            authEvent.put("userId", 456L);
            authEvent.put("sessionId", "session_xyz");
            authEvent.put("ip", "10.0.0.5");

            String message = objectMapper.writeValueAsString(authEvent);

            // When
            kafkaTemplate.send("auth.events", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);

                AuditEvent event = events.get(0);
                assertThat(event.getEventType()).isEqualTo(AuditEvent.EventType.LOGIN_FAILED);
                assertThat(event.getResult()).isEqualTo(AuditEvent.AuditResult.FAILURE);
                assertThat(event.getRiskScore()).isEqualTo(0.4);
            });
        }

        @Test
        @DisplayName("Should consume MFA_REQUIRED event")
        void shouldConsumeMfaRequiredEvent() throws Exception {
            // Given
            Map<String, Object> authEvent = new HashMap<>();
            authEvent.put("eventType", "MFA_REQUIRED");
            authEvent.put("userId", 789L);
            authEvent.put("sessionId", "session_mfa");

            String message = objectMapper.writeValueAsString(authEvent);

            // When
            kafkaTemplate.send("auth.events", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);
                assertThat(events.get(0).getEventType()).isEqualTo(AuditEvent.EventType.MFA_REQUIRED);
            });
        }

        @Test
        @DisplayName("Should consume NEW_DEVICE event with elevated risk")
        void shouldConsumeNewDeviceEvent() throws Exception {
            // Given
            Map<String, Object> authEvent = new HashMap<>();
            authEvent.put("eventType", "NEW_DEVICE");
            authEvent.put("userId", 111L);
            authEvent.put("device", "iPhone/Safari");

            String message = objectMapper.writeValueAsString(authEvent);

            // When
            kafkaTemplate.send("auth.events", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);

                AuditEvent event = events.get(0);
                assertThat(event.getEventType()).isEqualTo(AuditEvent.EventType.NEW_DEVICE);
                assertThat(event.getRiskScore()).isEqualTo(0.6);
            });
        }
    }

    // ==========================================
    // USER SERVICE EVENTS TESTS
    // ==========================================
    @Nested
    @DisplayName("User Service Events")
    class UserServiceTests {

        @Test
        @DisplayName("Should consume USER_CREATED event")
        void shouldConsumeUserCreatedEvent() throws Exception {
            // Given
            Map<String, Object> userEvent = new HashMap<>();
            userEvent.put("userId", 222L);
            userEvent.put("email", "test@example.com");
            userEvent.put("status", "ACTIVE");

            String message = objectMapper.writeValueAsString(userEvent);

            // When
            kafkaTemplate.send("user.created", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);

                AuditEvent event = events.get(0);
                assertThat(event.getEventType()).isEqualTo(AuditEvent.EventType.USER_CREATED);
                assertThat(event.getServiceSource()).isEqualTo("user-service");
                assertThat(event.getResourceType()).isEqualTo("User");
                assertThat(event.getComplianceFlags()).contains("GDPR");
            });
        }

        @Test
        @DisplayName("Should consume KYC_STATUS_CHANGED event with compliance flags")
        void shouldConsumeKycStatusChangedEvent() throws Exception {
            // Given
            Map<String, Object> kycEvent = new HashMap<>();
            kycEvent.put("userId", 333L);
            kycEvent.put("previousStatus", "PENDING");
            kycEvent.put("newStatus", "APPROVED");

            String message = objectMapper.writeValueAsString(kycEvent);

            // When
            kafkaTemplate.send("user.kyc-status-changed", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);

                AuditEvent event = events.get(0);
                assertThat(event.getEventType()).isEqualTo(AuditEvent.EventType.KYC_UPDATED);
                assertThat(event.getComplianceFlags()).containsExactlyInAnyOrder("GDPR", "KYC", "PSD2");
                assertThat(event.getChangesBefore()).isEqualTo("PENDING");
                assertThat(event.getChangesAfter()).isEqualTo("APPROVED");
            });
        }
    }

    // ==========================================
    // PAYMENT SERVICE EVENTS TESTS
    // ==========================================
    @Nested
    @DisplayName("Payment Service Events")
    class PaymentServiceTests {

        @Test
        @DisplayName("Should consume PAYMENT_COMPLETED event")
        void shouldConsumePaymentCompletedEvent() throws Exception {
            // Given
            Map<String, Object> paymentEvent = new HashMap<>();
            paymentEvent.put("paymentId", "pay_123");
            paymentEvent.put("userId", 456L);
            paymentEvent.put("amount", 150.00);
            paymentEvent.put("status", "COMPLETED");

            String message = objectMapper.writeValueAsString(paymentEvent);

            // When
            kafkaTemplate.send("payment.completed", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);

                AuditEvent event = events.get(0);
                assertThat(event.getEventType()).isEqualTo(AuditEvent.EventType.PAYMENT_COMPLETED);
                assertThat(event.getResourceType()).isEqualTo("Payment");
                assertThat(event.getResourceId()).isEqualTo("pay_123");
                assertThat(event.getResult()).isEqualTo(AuditEvent.AuditResult.SUCCESS);
                assertThat(event.getRiskScore()).isEqualTo(0.0);
            });
        }

        @Test
        @DisplayName("Should consume PAYMENT_FAILED event with elevated risk")
        void shouldConsumePaymentFailedEvent() throws Exception {
            // Given
            Map<String, Object> paymentEvent = new HashMap<>();
            paymentEvent.put("paymentId", "pay_456");
            paymentEvent.put("userId", 789L);
            paymentEvent.put("amount", 500.00);
            paymentEvent.put("status", "FAILED");

            String message = objectMapper.writeValueAsString(paymentEvent);

            // When
            kafkaTemplate.send("payment.failed", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);

                AuditEvent event = events.get(0);
                assertThat(event.getEventType()).isEqualTo(AuditEvent.EventType.PAYMENT_FAILED);
                assertThat(event.getResult()).isEqualTo(AuditEvent.AuditResult.FAILURE);
                assertThat(event.getRiskScore()).isEqualTo(0.3);
            });
        }

        @Test
        @DisplayName("Should consume FRAUD_DETECTED event with maximum risk")
        void shouldConsumeFraudDetectedEvent() throws Exception {
            // Given
            Map<String, Object> fraudEvent = new HashMap<>();
            fraudEvent.put("transactionId", "txn_fraud_001");
            fraudEvent.put("userId", 999L);
            fraudEvent.put("amount", 10000.00);
            fraudEvent.put("reason", "Suspicious pattern detected");

            String message = objectMapper.writeValueAsString(fraudEvent);

            // When
            kafkaTemplate.send("payment.fraud-detected", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);

                AuditEvent event = events.get(0);
                assertThat(event.getEventType()).isEqualTo(AuditEvent.EventType.FRAUD_DETECTED);
                assertThat(event.getResult()).isEqualTo(AuditEvent.AuditResult.BLOCKED);
                assertThat(event.getRiskScore()).isEqualTo(0.95);
                assertThat(event.getComplianceFlags()).containsExactlyInAnyOrder("FINANCIAL", "PSD2", "FRAUD");
            });
        }
    }

    // ==========================================
    // CRYPTO SERVICE EVENTS TESTS
    // ==========================================
    @Nested
    @DisplayName("Crypto Service Events")
    class CryptoServiceTests {

        @Test
        @DisplayName("Should consume CRYPTO_PURCHASE event")
        void shouldConsumeCryptoPurchaseEvent() throws Exception {
            // Given
            Map<String, Object> cryptoEvent = new HashMap<>();
            cryptoEvent.put("transactionId", "crypto_001");
            cryptoEvent.put("userId", 555L);
            cryptoEvent.put("transactionType", "BUY");
            cryptoEvent.put("cryptocurrency", "BTC");
            cryptoEvent.put("amount", 0.5);

            String message = objectMapper.writeValueAsString(cryptoEvent);

            // When
            kafkaTemplate.send("crypto.transaction", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);

                AuditEvent event = events.get(0);
                assertThat(event.getEventType()).isEqualTo(AuditEvent.EventType.CRYPTO_PURCHASE);
                assertThat(event.getAction()).isEqualTo("CRYPTO_BUY");
                assertThat(event.getResourceType()).isEqualTo("CryptoTransaction");
                assertThat(event.getComplianceFlags()).containsExactlyInAnyOrder("CRYPTO", "FINANCIAL");
                assertThat(event.getRiskScore()).isEqualTo(0.3);
            });
        }

        @Test
        @DisplayName("Should consume CRYPTO_SALE event")
        void shouldConsumeCryptoSaleEvent() throws Exception {
            // Given
            Map<String, Object> cryptoEvent = new HashMap<>();
            cryptoEvent.put("transactionId", "crypto_002");
            cryptoEvent.put("userId", 666L);
            cryptoEvent.put("transactionType", "SELL");
            cryptoEvent.put("cryptocurrency", "ETH");

            String message = objectMapper.writeValueAsString(cryptoEvent);

            // When
            kafkaTemplate.send("crypto.transaction", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);
                assertThat(events.get(0).getEventType()).isEqualTo(AuditEvent.EventType.CRYPTO_SALE);
                assertThat(events.get(0).getAction()).isEqualTo("CRYPTO_SELL");
            });
        }
    }

    // ==========================================
    // EDGE CASES & ERROR HANDLING
    // ==========================================
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle userId as String and convert to Long")
        void shouldHandleUserIdAsString() throws Exception {
            // Given
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "LOGIN_SUCCESS");
            event.put("userId", "123"); // String instead of Long
            event.put("sessionId", "session_string");

            String message = objectMapper.writeValueAsString(event);

            // When
            kafkaTemplate.send("auth.events", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);
                assertThat(events.get(0).getUserId()).isEqualTo(123L);
            });
        }

        @Test
        @DisplayName("Should handle missing optional fields gracefully")
        void shouldHandleMissingOptionalFields() throws Exception {
            // Given
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "LOGIN_SUCCESS");
            event.put("userId", 999L);
            // Missing: sessionId, ip, device, location

            String message = objectMapper.writeValueAsString(event);

            // When
            kafkaTemplate.send("auth.events", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);

                AuditEvent auditEvent = events.get(0);
                assertThat(auditEvent.getUserId()).isEqualTo(999L);
                assertThat(auditEvent.getSessionId()).isNull();
                assertThat(auditEvent.getIpAddress()).isNull();
            });
        }

        @Test
        @DisplayName("Should handle multiple field name alternatives")
        void shouldHandleFieldNameAlternatives() throws Exception {
            // Given - using alternative field name "ownerId" instead of "userId"
            Map<String, Object> accountEvent = new HashMap<>();
            accountEvent.put("accountId", "acc_123");
            accountEvent.put("ownerId", 777L); // alternative to userId
            accountEvent.put("balance", 1000.00);

            String message = objectMapper.writeValueAsString(accountEvent);

            // When
            kafkaTemplate.send("account.created", message);

            // Then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<AuditEvent> events = repository.findAll();
                assertThat(events).hasSize(1);
                assertThat(events.get(0).getUserId()).isEqualTo(777L);
            });
        }
    }

    // ==========================================
    // PERFORMANCE & CONCURRENCY TESTS
    // ==========================================
    @Nested
    @DisplayName("Performance and Concurrency")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle multiple concurrent events")
        void shouldHandleMultipleConcurrentEvents() throws Exception {
            // Given
            int eventCount = 5;

            // When - Send multiple events rapidly
            for (int i = 0; i < eventCount; i++) {
                Map<String, Object> event = new HashMap<>();
                event.put("eventType", "LOGIN_SUCCESS");
                event.put("userId", 1000L + i);
                event.put("sessionId", "session_" + i);

                String message = objectMapper.writeValueAsString(event);
                kafkaTemplate.send("auth.events", message);
            }

            // Then
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                long count = repository.count();
                assertThat(count).isEqualTo(eventCount);
            });
        }
    }
}