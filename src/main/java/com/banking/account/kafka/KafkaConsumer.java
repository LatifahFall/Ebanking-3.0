// ============================================================================
// KafkaConsumer.java – Version finale, 100% compatible payment-service
// ============================================================================
package com.banking.account.kafka;

import com.banking.account.kafka.event.FraudDetectedEvent;
import com.banking.account.kafka.event.PaymentCompletedEvent;
import com.banking.account.kafka.event.PaymentReversedEvent;
import com.banking.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final AccountService accountService;

    // ========================================================================
    // 1. Paiement complété → Mise à jour du solde (débit ou crédit)
    // ========================================================================
    @KafkaListener(
            topics = "${app.kafka.topics.payment-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePaymentCompleted(
            @Payload PaymentCompletedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        log.info("[PAYMENT-COMPLETED] ➜ Received | paymentId: {} | accountId: {} | amount: {} {} | offset: {}",
                event.getPaymentId(), event.getAccountId(), event.getAmount(), event.getCurrency(), offset);

        try {
            accountService.processPaymentCompleted(event);
            ack.acknowledge();
            log.info("[PAYMENT-COMPLETED] ✅ Successfully applied to account {}", event.getAccountId());

        } catch (Exception e) {
            log.error("[PAYMENT-COMPLETED] ❌ Failed to process | paymentId: {} | accountId: {}",
                    event.getPaymentId(), event.getAccountId(), e);
            // L'exception est relancée pour que Spring Kafka applique retry + DLQ via ErrorHandler
            throw e;
        }
    }

    // ========================================================================
    // 2. Paiement reversé → Remboursement (crédit)
    // ========================================================================
    @KafkaListener(
            topics = "${app.kafka.topics.payment-reversed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePaymentReversed(
            @Payload PaymentReversedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        log.info("[PAYMENT-REVERSED] ➜ Received | paymentId: {} | accountId: {} | amount: {} | reason: {}",
                event.getPaymentId(), event.getAccountId(), event.getAmount(), event.getReversalReason());

        try {
            accountService.processPaymentReversed(event);
            ack.acknowledge();
            log.info("[PAYMENT-REVERSED] ✅ Reversal applied | accountId: {}", event.getAccountId());

        } catch (Exception e) {
            log.error("[PAYMENT-REVERSED] ❌ Failed to process reversal | paymentId: {}", event.getPaymentId(), e);
            throw e;
        }
    }

    // ========================================================================
    // 3. Fraude détectée → Suspension automatique du compte (recommandé par payment-service)
    // ========================================================================
    @KafkaListener(
            topics = "${app.kafka.topics.fraud-detected}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleFraudDetected(
            @Payload FraudDetectedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) Long offset,
            Acknowledgment ack) {

        log.warn("⚠️ [FRAUD-DETECTED] ➜ Alert received | fraudId: {} | paymentId: {} | accountId: {} | amount: {} | action: {}",
                event.getFraudId(), event.getPaymentId(), event.getAccountId(), event.getAmount(), event.getAction());

        try {
            // Suspension uniquement si l'action demandée est BLOCKED
            if ("BLOCKED".equalsIgnoreCase(event.getAction())) {
                accountService.suspendAccountForFraud(event.getAccountId().getMostSignificantBits(), event.getReason());                log.warn("🔴 Account {} automatically SUSPENDED due to fraud detection", event.getAccountId());
            } else {
                log.info("[FRAUD-DETECTED] Action is PENDING_REVIEW → no automatic suspension | accountId: {}", event.getAccountId());
            }

            ack.acknowledge();

        } catch (Exception e) {
            log.error("[FRAUD-DETECTED] ❌ Critical failure handling fraud alert | fraudId: {}", event.getFraudId(), e);
            throw e;
        }
    }
}