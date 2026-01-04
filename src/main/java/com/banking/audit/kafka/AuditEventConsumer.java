package com.banking.audit.kafka;

import com.banking.audit.model.AuditEvent;
import com.banking.audit.service.AuditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("kafka")
public class AuditEventConsumer {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    // =======================================
    // 1. AUTH SERVICE EVENTS
    // =======================================
    @KafkaListener(topics = "${audit.topics.auth-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAuthEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        processEvent(message, topic, key, ack, json -> {
            String eventTypeStr = getFieldAsText(json, "eventType");
            AuditEvent.EventType eventType = mapAuthEventType(eventTypeStr);

            Set<String> complianceFlags = Set.of("SECURITY", "AUTHENTICATION");
            double riskScore = calculateAuthRiskScore(eventTypeStr);

            return AuditEvent.builder()
                    .eventType(eventType)
                    .userId(getFieldAsLong(json, "userId"))
                    .sessionId(getFieldAsText(json, "sessionId"))
                    .serviceSource("auth-service")
                    .action(eventTypeStr)
                    .ipAddress(getFieldAsText(json, "ip", "ipAddress"))
                    .userAgent(getFieldAsText(json, "device", "userAgent"))
                    .geolocation(getFieldAsText(json, "location", "geolocation"))
                    .timestamp(LocalDateTime.now())
                    .result(determineAuthResult(eventTypeStr))
                    .complianceFlags(complianceFlags)
                    .riskScore(riskScore)
                    .sensitive(true)
                    .build();
        });
    }

    // =======================================
    // 2. USER SERVICE EVENTS
    // =======================================
    @KafkaListener(topics = {
            "${audit.topics.user-created}",
            "${audit.topics.user-updated}",
            "${audit.topics.user-activated}",
            "${audit.topics.user-deactivated}",
            "${audit.topics.kyc-status-changed}",
            "${audit.topics.client-assigned}",
            "${audit.topics.client-unassigned}"
    }, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        processEvent(message, topic, key, ack, json -> {
            AuditEvent.EventType eventType = mapUserEventType(topic);
            Set<String> complianceFlags = Set.of("GDPR");

            if (topic.contains("kyc") || topic.contains("client")) {
                complianceFlags = Set.of("GDPR", "KYC", "PSD2");
            }

            return AuditEvent.builder()
                    .eventType(eventType)
                    .userId(getFieldAsLong(json, "userId", "id"))
                    .serviceSource("user-service")
                    .action(extractActionFromTopic(topic))
                    .resourceType("User")
                    .resourceId(getFieldAsText(json, "userId", "id"))
                    .resourceDetails(message)
                    .changesBefore(getFieldAsText(json, "previousStatus", "oldValue"))
                    .changesAfter(getFieldAsText(json, "newStatus", "newValue"))
                    .timestamp(LocalDateTime.now())
                    .result(AuditEvent.AuditResult.SUCCESS)
                    .complianceFlags(complianceFlags)
                    .sensitive(true)
                    .build();
        });
    }

    // =======================================
    // 3. ACCOUNT SERVICE EVENTS
    // =======================================
    @KafkaListener(topics = {
            "${audit.topics.account-created}",
            "${audit.topics.account-updated}",
            "${audit.topics.account-balance-changed}",
            "${audit.topics.account-suspended}",
            "${audit.topics.account-closed}",
            "${audit.topics.transaction-completed}"
    }, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAccountEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        processEvent(message, topic, key, ack, json -> {
            AuditEvent.EventType eventType = mapAccountEventType(topic);

            Set<String> complianceFlags = Set.of("FINANCIAL", "PSD2");
            if (topic.contains("transaction-completed")) {
                eventType = AuditEvent.EventType.TRANSACTION_COMPLETED;
                complianceFlags = Set.of("FINANCIAL", "PSD2", "TRANSACTION");
            }

            return AuditEvent.builder()
                    .eventType(eventType)
                    .userId(getFieldAsLong(json, "userId", "ownerId"))
                    .serviceSource("account-service")
                    .action(extractActionFromTopic(topic))
                    .resourceType(topic.contains("transaction") ? "Transaction" : "Account")
                    .resourceId(getFieldAsText(json, "transactionId", "accountId", "id"))
                    .resourceDetails(message)
                    .timestamp(LocalDateTime.now())
                    .result(AuditEvent.AuditResult.SUCCESS)
                    .complianceFlags(complianceFlags)
                    .sensitive(true)
                    .build();
        });
    }

    // =======================================
    // 4. PAYMENT SERVICE EVENTS
    // =======================================
    @KafkaListener(topics = {
            "${audit.topics.payment-initiated}",
            "${audit.topics.payment-completed}",
            "${audit.topics.payment-failed}",
            "${audit.topics.payment-reversed}",
            "${audit.topics.fraud-detected}"
    }, groupId = "${spring.kafka.consumer.group-id}")
    public void consumePaymentEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        processEvent(message, topic, key, ack, json -> {
            AuditEvent.EventType eventType = mapPaymentEventType(topic);
            Set<String> complianceFlags = Set.of("FINANCIAL", "PSD2");
            double riskScore = 0.0;
            AuditEvent.AuditResult result = AuditEvent.AuditResult.SUCCESS;

            if (topic.contains("fraud")) {
                complianceFlags = Set.of("FINANCIAL", "PSD2", "FRAUD");
                riskScore = 0.95;
                result = AuditEvent.AuditResult.BLOCKED;
            } else if (topic.contains("failed")) {
                result = AuditEvent.AuditResult.FAILURE;
                riskScore = 0.3;
            }

            return AuditEvent.builder()
                    .eventType(eventType)
                    .userId(getFieldAsLong(json, "userId", "senderId"))
                    .serviceSource("payment-service")
                    .action(extractActionFromTopic(topic))
                    .resourceType("Payment")
                    .resourceId(getFieldAsText(json, "paymentId", "transactionId", "id"))
                    .resourceDetails(message)
                    .timestamp(LocalDateTime.now())
                    .result(result)
                    .complianceFlags(complianceFlags)
                    .riskScore(riskScore)
                    .sensitive(true)
                    .build();
        });
    }

    // =======================================
    // 5. CRYPTO SERVICE EVENTS
    // =======================================
    @KafkaListener(topics = "${audit.topics.crypto-transaction}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeCryptoEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        processEvent(message, topic, key, ack, json -> {
            String transactionType = getFieldAsText(json, "transactionType", "type");
            AuditEvent.EventType eventType = "BUY".equalsIgnoreCase(transactionType) ?
                    AuditEvent.EventType.CRYPTO_PURCHASE : AuditEvent.EventType.CRYPTO_SALE;

            return AuditEvent.builder()
                    .eventType(eventType)
                    .userId(getFieldAsLong(json, "userId", "ownerId"))
                    .serviceSource("crypto-service")
                    .action("CRYPTO_" + (transactionType != null ? transactionType.toUpperCase() : "UNKNOWN"))
                    .resourceType("CryptoTransaction")
                    .resourceId(getFieldAsText(json, "transactionId", "id"))
                    .resourceDetails(message)
                    .timestamp(LocalDateTime.now())
                    .result(AuditEvent.AuditResult.SUCCESS)
                    .complianceFlags(Set.of("CRYPTO", "FINANCIAL"))
                    .riskScore(0.3)
                    .sensitive(true)
                    .build();
        });
    }

    // =======================================
    // 6. NOTIFICATION SERVICE EVENTS
    // =======================================
    @KafkaListener(topics = {
            "${audit.topics.notification-status}",
            "${audit.topics.notification-audit}"
    }, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeNotificationEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        processEvent(message, topic, key, ack, json -> {
            String eventTypeStr = getFieldAsText(json, "eventType", "status");
            AuditEvent.EventType eventType = eventTypeStr != null && eventTypeStr.contains("FAILED") ?
                    AuditEvent.EventType.NOTIFICATION_FAILED :
                    AuditEvent.EventType.NOTIFICATION_SENT;

            return AuditEvent.builder()
                    .eventType(eventType)
                    .userId(getFieldAsLong(json, "userId", "recipientId"))
                    .serviceSource("notification-service")
                    .action(eventTypeStr != null ? eventTypeStr : "NOTIFICATION")
                    .resourceType("Notification")
                    .resourceId(getFieldAsText(json, "notificationId", "eventId", "id"))
                    .resourceDetails(message)
                    .timestamp(LocalDateTime.now())
                    .result(eventType == AuditEvent.EventType.NOTIFICATION_FAILED ?
                            AuditEvent.AuditResult.FAILURE : AuditEvent.AuditResult.SUCCESS)
                    .complianceFlags(Set.of("NOTIFICATION"))
                    .build();
        });
    }

    // =======================================
    // HELPER: Process Event (centralisé + DLQ ready)
    // =======================================
    private void processEvent(String message, String topic, String key, Acknowledgment ack, EventBuilder builder) {
        try {
            log.debug("🔍 [AUDIT] Processing event | topic={} | key={}", topic, key);
            JsonNode json = objectMapper.readTree(message);
            AuditEvent auditEvent = builder.build(json);

            auditService.saveAuditEvent(auditEvent);
            ack.acknowledge();
            log.debug("✅ [AUDIT] Event processed | topic={}", topic);

        } catch (Exception e) {
            log.error("❌ [AUDIT] Failed to process event | topic={} | key={} | error={}", topic, key, e.getMessage(), e);
            // Throw → Spring Kafka retry + DLQ
            throw new RuntimeException("Audit processing failed", e);
        }
    }

    @FunctionalInterface
    interface EventBuilder {
        AuditEvent build(JsonNode json);
    }

    // =======================================
    // MAPPING HELPERS
    // =======================================

    // ✅ NOUVEAU: Helper pour récupérer Long
    private Long getFieldAsLong(JsonNode node, String... fieldNames) {
        for (String name : fieldNames) {
            JsonNode field = node.get(name);
            if (field != null && !field.isNull()) {
                if (field.isNumber()) {
                    return field.asLong();
                } else if (field.isTextual()) {
                    try {
                        return Long.parseLong(field.asText());
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse field {} as Long: {}", name, field.asText());
                    }
                }
            }
        }
        return null;
    }

    private String getFieldAsText(JsonNode node, String... fieldNames) {
        for (String name : fieldNames) {
            JsonNode field = node.get(name);
            if (field != null && !field.isNull()) {
                return field.asText(null);
            }
        }
        return null;
    }

    private String extractActionFromTopic(String topic) {
        String[] parts = topic.split("\\.");
        return parts[parts.length - 1].toUpperCase().replace("-", "_");
    }

    private AuditEvent.EventType mapAuthEventType(String eventType) {
        if (eventType == null) return AuditEvent.EventType.USER_LOGIN;
        return switch (eventType.toUpperCase()) {
            case "LOGIN_SUCCESS", "LOGIN" -> AuditEvent.EventType.USER_LOGIN;
            case "LOGOUT" -> AuditEvent.EventType.USER_LOGOUT;
            case "LOGIN_FAILED", "FAILED" -> AuditEvent.EventType.LOGIN_FAILED;
            case "MFA_REQUIRED" -> AuditEvent.EventType.MFA_REQUIRED;
            case "MFA_SUCCESS" -> AuditEvent.EventType.MFA_SUCCESS;
            case "MFA_FAILED" -> AuditEvent.EventType.MFA_FAILED;
            case "PASSWORD_RESET" -> AuditEvent.EventType.PASSWORD_RESET;
            case "NEW_DEVICE" -> AuditEvent.EventType.NEW_DEVICE;
            case "ACCESS_DENIED" -> AuditEvent.EventType.ACCESS_DENIED;
            default -> AuditEvent.EventType.USER_LOGIN;
        };
    }

    private AuditEvent.EventType mapUserEventType(String topic) {
        if (topic.contains("created")) return AuditEvent.EventType.USER_CREATED;
        if (topic.contains("updated")) return AuditEvent.EventType.USER_UPDATED;
        if (topic.contains("activated")) return AuditEvent.EventType.USER_ACTIVATED;
        if (topic.contains("deactivated")) return AuditEvent.EventType.USER_DEACTIVATED;
        if (topic.contains("kyc")) return AuditEvent.EventType.KYC_UPDATED;
        if (topic.contains("assigned")) return AuditEvent.EventType.CLIENT_ASSIGNED;
        if (topic.contains("unassigned")) return AuditEvent.EventType.CLIENT_UNASSIGNED;
        return AuditEvent.EventType.USER_UPDATED;
    }

    private AuditEvent.EventType mapAccountEventType(String topic) {
        if (topic.contains("created")) return AuditEvent.EventType.ACCOUNT_CREATED;
        if (topic.contains("updated")) return AuditEvent.EventType.ACCOUNT_UPDATED;
        if (topic.contains("balance")) return AuditEvent.EventType.ACCOUNT_BALANCE_CHANGED;
        if (topic.contains("suspended")) return AuditEvent.EventType.ACCOUNT_SUSPENDED;
        if (topic.contains("closed")) return AuditEvent.EventType.ACCOUNT_CLOSED;
        if (topic.contains("transaction-completed")) return AuditEvent.EventType.TRANSACTION_COMPLETED;
        return AuditEvent.EventType.ACCOUNT_UPDATED;
    }

    private AuditEvent.EventType mapPaymentEventType(String topic) {
        if (topic.contains("initiated")) return AuditEvent.EventType.PAYMENT_INITIATED;
        if (topic.contains("completed")) return AuditEvent.EventType.PAYMENT_COMPLETED;
        if (topic.contains("failed")) return AuditEvent.EventType.PAYMENT_FAILED;
        if (topic.contains("reversed")) return AuditEvent.EventType.PAYMENT_REVERSED;
        if (topic.contains("fraud")) return AuditEvent.EventType.FRAUD_DETECTED;
        return AuditEvent.EventType.PAYMENT_COMPLETED;
    }

    private AuditEvent.AuditResult determineAuthResult(String eventType) {
        if (eventType == null) return AuditEvent.AuditResult.SUCCESS;
        return eventType.toUpperCase().contains("FAILED") || eventType.toUpperCase().contains("DENIED") ?
                AuditEvent.AuditResult.FAILURE : AuditEvent.AuditResult.SUCCESS;
    }

    private double calculateAuthRiskScore(String eventType) {
        if (eventType == null) return 0.1;
        return switch (eventType.toUpperCase()) {
            case "LOGIN_FAILED", "FAILED" -> 0.4;
            case "NEW_DEVICE" -> 0.6;
            case "MFA_FAILED" -> 0.7;
            case "ACCESS_DENIED" -> 0.8;
            default -> 0.1;
        };
    }
}