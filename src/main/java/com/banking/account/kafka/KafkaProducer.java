// ============================================================================
// KafkaProducer.java
// ============================================================================
package com.banking.account.kafka;

import com.banking.account.kafka.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Topics Producer Configuration
    @Value("${app.kafka.topics.account-created}")
    private String accountCreatedTopic;

    @Value("${app.kafka.topics.account-updated}")
    private String accountUpdatedTopic;

    @Value("${app.kafka.topics.account-balance-changed}")
    private String balanceChangedTopic;

    @Value("${app.kafka.topics.account-suspended}")
    private String accountSuspendedTopic;

    @Value("${app.kafka.topics.account-closed}")
    private String accountClosedTopic;

    /**
     * Producer 1: account.created 🎉 [OBLIGATOIRE]
     * Consommé par: Notification Service, Payment Service, Analytics Service
     */
    public void publishAccountCreated(AccountCreatedEvent event) {
        log.info("📤 [ACCOUNT-CREATED] Publishing event for accountId: {}, type: {}, currency: {}",
                event.getAccountId(), event.getAccountType(), event.getCurrency());

        sendEvent(accountCreatedTopic, event.getAccountId().toString(), event, "AccountCreated");
    }

    /**
     * Producer 2: account.updated 🔄 [OBLIGATOIRE]
     * Consommé par: Payment Service, Card Service
     */
    public void publishAccountUpdated(AccountUpdatedEvent event) {
        log.info("📤 [ACCOUNT-UPDATED] Publishing event for accountId: {}, status: {} -> {}",
                event.getAccountId(), event.getPreviousStatus(), event.getNewStatus());

        sendEvent(accountUpdatedTopic, event.getAccountId().toString(), event, "AccountUpdated");
    }

    /**
     * Producer 3: account.balance.changed 💰 [OBLIGATOIRE]
     * Consommé par: Analytics Service, Notification Service
     */
    public void publishBalanceChanged(BalanceChangedEvent event) {
        log.info("📤 [BALANCE-CHANGED] Publishing event for accountId: {}, balance: {} -> {} ({}{})",
                event.getAccountId(),
                event.getPreviousBalance(),
                event.getNewBalance(),
                event.getChangeType() == ChangeType.DEBIT ? "-" : "+",
                event.getChangeAmount());

        sendEvent(balanceChangedTopic, event.getAccountId().toString(), event, "BalanceChanged");
    }

    /**
     * Producer 4: account.suspended 🚫 [OBLIGATOIRE]
     * Consommé par: Payment Service, Card Service, Notification Service
     */
    public void publishAccountSuspended(AccountSuspendedEvent event) {
        log.warn("📤 [ACCOUNT-SUSPENDED] Publishing event for accountId: {}, reason: {}, by: {}",
                event.getAccountId(), event.getSuspensionReason(), event.getSuspendedBy());

        sendEvent(accountSuspendedTopic, event.getAccountId().toString(), event, "AccountSuspended");
    }

    /**
     * Producer 5: account.closed 🔒 [OBLIGATOIRE]
     * Consommé par: Payment Service, Card Service, Notification Service
     */
    public void publishAccountClosed(AccountClosedEvent event) {
        log.warn("📤 [ACCOUNT-CLOSED] Publishing event for accountId: {}, reason: {}, finalBalance: {}",
                event.getAccountId(), event.getClosureReason(), event.getFinalBalance());

        sendEvent(accountClosedTopic, event.getAccountId().toString(), event, "AccountClosed");
    }

    /**
     * Generic method to send events with proper error handling
     */
    private void sendEvent(String topic, String key, Object event, String eventType) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                var metadata = result.getRecordMetadata();
                log.info("✅ [{}] Event sent successfully to topic: {}, partition: {}, offset: {}",
                        eventType, metadata.topic(), metadata.partition(), metadata.offset());
            } else {
                log.error("❌ [{}] Failed to send event to topic: {}, key: {}",
                        eventType, topic, key, ex);
                // TODO: Implement retry logic or alert monitoring system
            }
        });
    }

    /**
     * Synchronous send for critical operations (if needed)
     */
    public void publishAccountCreatedSync(AccountCreatedEvent event) throws Exception {
        log.info("📤 [ACCOUNT-CREATED-SYNC] Publishing event synchronously for accountId: {}",
                event.getAccountId());

        SendResult<String, Object> result = kafkaTemplate
                .send(accountCreatedTopic, event.getAccountId().toString(), event)
                .get(); // Blocking call

        log.info("✅ [ACCOUNT-CREATED-SYNC] Event sent with offset: {}",
                result.getRecordMetadata().offset());
    }
}
