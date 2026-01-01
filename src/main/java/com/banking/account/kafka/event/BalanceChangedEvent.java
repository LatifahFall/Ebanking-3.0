// BalanceChangedEvent - CORRIGÉ
package com.banking.account.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceChangedEvent {
    private Long accountId;
    private BigDecimal previousBalance; // ⭐ RENOMMÉ depuis oldBalance
    private BigDecimal newBalance;
    private BigDecimal changeAmount; // ⭐ RENOMMÉ depuis amountChanged
    private ChangeType changeType; // ⭐ AJOUTÉ (CREDIT ou DEBIT)
    private String transactionReference; // ⭐ RENOMMÉ depuis reason
    private LocalDateTime timestamp; // ⭐ AJOUTÉ (optionnel)
}