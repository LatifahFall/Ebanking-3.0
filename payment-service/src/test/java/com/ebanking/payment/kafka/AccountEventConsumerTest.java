package com.ebanking.payment.kafka;

import com.ebanking.payment.client.AccountStatus;
import com.ebanking.payment.kafka.event.AccountCreatedEvent;
import com.ebanking.payment.kafka.event.AccountUpdatedEvent;
import com.ebanking.payment.service.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountEventConsumerTest {

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private AccountEventConsumer accountEventConsumer;

    private UUID accountId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void shouldConsumeAccountCreatedEvent() {
        AccountCreatedEvent event = AccountCreatedEvent.builder()
                .accountId(accountId)
                .userId(userId)
                .accountNumber("ACC001")
                .accountType("SAVINGS")
                .currency("EUR")
                .balance(new BigDecimal("1000.00"))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        accountEventConsumer.consumeAccountCreated(event, "account.created", acknowledgment);

        verify(acknowledgment).acknowledge();
        
        AccountEventConsumer.AccountCacheEntry cached = accountEventConsumer.getAccountFromCache(accountId);
        assertThat(cached).isNotNull();
        assertThat(cached.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void shouldConsumeAccountUpdatedEventAndAddToBlacklist() {
        AccountUpdatedEvent event = AccountUpdatedEvent.builder()
                .accountId(accountId)
                .userId(userId)
                .accountNumber("ACC001")
                .accountType("SAVINGS")
                .currency("EUR")
                .status("SUSPENDED")
                .updatedAt(LocalDateTime.now())
                .changedFields(List.of("status"))
                .build();

        accountEventConsumer.consumeAccountUpdated(event, "account.updated", acknowledgment);

        verify(fraudDetectionService).addToBlacklist(accountId);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldRemoveFromBlacklistWhenAccountBecomesActive() {
        AccountUpdatedEvent event = AccountUpdatedEvent.builder()
                .accountId(accountId)
                .userId(userId)
                .accountNumber("ACC001")
                .status("ACTIVE")
                .updatedAt(LocalDateTime.now())
                .changedFields(List.of("status"))
                .build();

        accountEventConsumer.consumeAccountUpdated(event, "account.updated", acknowledgment);

        verify(fraudDetectionService).removeFromBlacklist(accountId);
        verify(acknowledgment).acknowledge();
    }
}

