// AccountCreatedEvent - CORRIGÉ
package com.banking.account.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreatedEvent {
    private Long accountId;
    private String accountNumber;
    private Long userId;
    private String accountType; // ⭐ String au lieu de enum
    private String currency;
    private BigDecimal initialBalance;
    private String status; // ⭐ AJOUTÉ
    private Instant createdAt; // ⭐ Instant au lieu de LocalDateTime
    private String accountName; // ⭐ AJOUTÉ (optionnel)
}