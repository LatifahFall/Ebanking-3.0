package com.ebanking.payment.kafka;

import com.ebanking.payment.client.AccountStatus;
import com.ebanking.payment.kafka.event.AccountCreatedEvent;
import com.ebanking.payment.kafka.event.AccountUpdatedEvent;
import com.ebanking.payment.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventConsumer {

    private final FraudDetectionService fraudDetectionService;
    
    // Local cache for account status
    private final Map<UUID, AccountCacheEntry> accountCache = new ConcurrentHashMap<>();

    @KafkaListener(
            topics = "${payment.service.kafka.topics.consumer.account-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeAccountCreated(
            @Payload AccountCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        try {
            log.info("Received account.created event: {}", event.getAccountId());
            
            AccountCacheEntry cacheEntry = AccountCacheEntry.builder()
                    .accountId(event.getAccountId())
                    .userId(event.getUserId())
                    .accountNumber(event.getAccountNumber())
                    .status(AccountStatus.valueOf(event.getStatus()))
                    .build();
            
            accountCache.put(event.getAccountId(), cacheEntry);
            log.debug("Account {} cached", event.getAccountId());
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing account.created event for account: {}", event.getAccountId(), e);
            // Don't acknowledge - message will be retried
        }
    }

    @KafkaListener(
            topics = "${payment.service.kafka.topics.consumer.account-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeAccountUpdated(
            @Payload AccountUpdatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        try {
            log.info("Received account.updated event: {}", event.getAccountId());
            
            AccountStatus newStatus = AccountStatus.valueOf(event.getStatus());
            
            // Update cache
            AccountCacheEntry cacheEntry = accountCache.get(event.getAccountId());
            if (cacheEntry != null) {
                cacheEntry.setStatus(newStatus);
                log.debug("Account {} status updated in cache to {}", event.getAccountId(), newStatus);
            } else {
                // Create new entry if not in cache
                cacheEntry = AccountCacheEntry.builder()
                        .accountId(event.getAccountId())
                        .userId(event.getUserId())
                        .accountNumber(event.getAccountNumber())
                        .status(newStatus)
                        .build();
                accountCache.put(event.getAccountId(), cacheEntry);
            }
            
            // If account is suspended, add to blacklist
            if (newStatus == AccountStatus.SUSPENDED || newStatus == AccountStatus.INACTIVE) {
                fraudDetectionService.addToBlacklist(event.getAccountId());
                log.warn("Account {} added to blacklist due to status: {}", event.getAccountId(), newStatus);
            } else if (newStatus == AccountStatus.ACTIVE) {
                fraudDetectionService.removeFromBlacklist(event.getAccountId());
            }
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing account.updated event for account: {}", event.getAccountId(), e);
            // Don't acknowledge - message will be retried
        }
    }

    public AccountCacheEntry getAccountFromCache(UUID accountId) {
        return accountCache.get(accountId);
    }

    @lombok.Data
    @lombok.Builder
    public static class AccountCacheEntry {
        private UUID accountId;
        private UUID userId;
        private String accountNumber;
        private AccountStatus status;
    }
}

