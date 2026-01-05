package com.banking.analytics.kafka;

import com.banking.analytics.service.MetricsAggregationService;
import com.banking.analytics.service.AlertService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsEventConsumer {

    private final MetricsAggregationService metricsService;
    private final AlertService alertService;
    private final ObjectMapper objectMapper;

    // ========== PAYMENT EVENTS ==========

    @KafkaListener(topics = {
            "${analytics.topics.payment-completed}",
            "${analytics.topics.payment-failed}",

    }, groupId = "${spring.kafka.consumer.group-id}")
    public void consumePaymentEvents(@Payload String message,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     Acknowledgment ack) {
        try {
            log.debug("Received payment event from topic: {}", topic);
            JsonNode event = objectMapper.readTree(message);

            String userId = getFieldAsText(event, "userId");
            BigDecimal amount = getBigDecimalField(event, "amount");
            String status = topic.contains("failed") ? "FAILED" : "SUCCESS";

            // Aggregate transaction metrics
            metricsService.processTransaction(userId, amount, "PAYMENT", status);

            // Check for alerts
            if (amount.compareTo(new BigDecimal("1000")) > 0) {
                alertService.checkLargeTransaction(userId, amount);
            }

            alertService.checkSpendingThreshold(userId);

            ack.acknowledge();
            log.debug("Payment event processed successfully");

        } catch (Exception e) {
            log.error("Error processing payment event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    // ========== ACCOUNT EVENTS ==========

    @KafkaListener(topics = {
            "${analytics.topics.account-created}",
            "${analytics.topics.account-balance-changed}",
            "${analytics.topics.transaction-completed}"  // Add here
    }, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAccountEvents(@Payload String message,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     Acknowledgment ack) {
        try {
            log.debug("Received account-related event from topic: {}", topic);
            JsonNode event = objectMapper.readTree(message);

            String userId = getFieldAsText(event, "userId");
            String accountId = getFieldAsText(event, "accountId", "fromAccountId", "toAccountId");

            if (topic.contains("balance")) {
                BigDecimal newBalance = getBigDecimalField(event, "balance", "newBalance");
                metricsService.updateAccountBalance(userId, accountId, newBalance);

                if (newBalance.compareTo(new BigDecimal("100")) < 0) {
                    alertService.triggerLowBalanceAlert(userId, newBalance);
                }
            }

            if (topic.contains("created")) {
                metricsService.trackAccountCreation(userId, accountId);
            }

            if (topic.contains("transaction-completed")) {
                BigDecimal amount = getBigDecimalField(event, "amount");
                String transactionType = getFieldAsText(event, "type", "transactionType"); // e.g., TRANSFER, DEPOSIT, WITHDRAWAL

                // Use appropriate metric type, not hard-coded "PAYMENT"
                metricsService.processTransaction(userId, amount, transactionType != null ? transactionType : "TRANSFER", "SUCCESS");

                // Optional: different alerting rules for internal transactions
                if (amount.compareTo(new BigDecimal("5000")) > 0) {  // maybe higher threshold for internal transfers
                    alertService.checkLargeTransaction(userId, amount);
                }
            }

            ack.acknowledge();
            log.debug("Account event processed successfully");

        } catch (Exception e) {
            log.error("Error processing account event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }
    // ========================================
    // USER SERVICE EVENTS
    // ========================================

    @KafkaListener(topics = {
            "${analytics.topics.user-created}",
            "${analytics.topics.user-updated}",
            "${analytics.topics.user-activated}",
            "${analytics.topics.user-deactivated}",
            "${analytics.topics.kyc-status-changed}",
            "${analytics.topics.client-assigned}",
            "${analytics.topics.client-unassigned}"
    }, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserEvents(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  Acknowledgment ack) {
        try {
            log.debug("Received user event from topic: {}", topic);
            JsonNode event = objectMapper.readTree(message);

            String userId = getFieldAsText(event, "userId");

            if (topic.contains("created")) {
                log.info("New user created: {}", userId);
                // Track user registration in analytics
            }

            if (topic.contains("kyc")) {
                String newStatus = getFieldAsText(event, "newStatus", "status");
                log.info("KYC status changed for user {}: {}", userId, newStatus);
            }

            ack.acknowledge();
            log.debug("User event processed successfully");

        } catch (Exception e) {
            log.error("Error processing user event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }


    // ========== AUTH EVENTS ==========

    @KafkaListener(topics = "${analytics.topics.auth-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAuthEvents(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  Acknowledgment ack) {
        try {
            log.debug("Received auth event from topic: {}", topic);
            JsonNode event = objectMapper.readTree(message);

            String userId = getFieldAsText(event, "userId");
            String eventType = getFieldAsText(event, "eventType");

            if ("LOGIN_SUCCESS".equals(eventType)) {
                metricsService.trackLogin(userId);
            }

            if ("LOGIN_FAILED".equals(eventType)) {
                alertService.checkSuspiciousLogin(userId);
            }

            ack.acknowledge();
            log.debug("Auth event processed successfully");

        } catch (Exception e) {
            log.error("Error processing auth event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    // ========== CRYPTO EVENTS ==========

    @KafkaListener(topics = "${analytics.topics.crypto-transaction}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeCryptoEvents(@Payload String message,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    Acknowledgment ack) {
        try {
            log.debug("Received crypto event from topic: {}", topic);
            JsonNode event = objectMapper.readTree(message);

            String userId = getFieldAsText(event, "userId");
            BigDecimal fiatAmount = getBigDecimalField(event, "fiatAmount");
            String transactionType = getFieldAsText(event, "transactionType");

            metricsService.trackCryptoTransaction(userId, fiatAmount, transactionType);

            ack.acknowledge();
            log.debug("Crypto event processed successfully");

        } catch (Exception e) {
            log.error("Error processing crypto event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    // ========================================
    // NOTIFICATION SERVICE EVENTS
    // ========================================

    @KafkaListener(topics = {
            "${analytics.topics.notification-status}",
            "${analytics.topics.notification-audit}"
    }, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeNotificationEvents(@Payload String message,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                          Acknowledgment ack) {
        try {
            log.debug("Received notification event from topic: {}", topic);
            JsonNode event = objectMapper.readTree(message);

            String userId = getFieldAsText(event, "userId");
            String eventType = getFieldAsText(event, "eventType");

            if (topic.contains("status")) {
                String status = getFieldAsText(event, "status");

                if ("SENT".equals(status) || "NOTIFICATION_SENT".equals(eventType)) {
                    metricsService.trackNotification(userId);
                }

                if ("FAILED".equals(status) || "NOTIFICATION_FAILED".equals(eventType)) {
                    log.warn("Notification failed for user {}", userId);
                }
            }

            ack.acknowledge();
            log.debug("Notification event processed successfully");

        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private String getFieldAsText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field != null && !field.isNull()) {
                return field.asText();
            }
        }
        return null;
    }

    private BigDecimal getBigDecimalField(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field != null && !field.isNull()) {
                try {
                    return new BigDecimal(field.asText());
                } catch (NumberFormatException e) {
                    // Try as double
                    return BigDecimal.valueOf(field.asDouble());
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private String determinePaymentStatus(String topic) {
        if (topic.contains("failed")) {
            return "FAILED";
        }
        if (topic.contains("reversed")) {
            return "REVERSED";
        }
        if (topic.contains("completed")) {
            return "SUCCESS";
        }
        if (topic.contains("initiated")) {
            return "PENDING";
        }
        return "UNKNOWN";
    }


}