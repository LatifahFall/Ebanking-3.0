// ============================================================================
// Event Classes
// ============================================================================

// TransactionCompletedEvent.java
package com.banking.account.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompletedEvent {
    private UUID transactionId;
    private UUID accountId;
    private BigDecimal amount;
    private String currency;
    private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER
    private Instant completedAt;
}
