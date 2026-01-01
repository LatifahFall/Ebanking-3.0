// ==================== 7. TransactionResponse (Champs ajustés) ====================
package com.banking.account.dto;

import com.banking.account.model.Transaction;
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
public class TransactionResponse {
    private Long id;
    private Long accountId;
    private Transaction.TransactionType type; // ⭐ CORRIGÉ: utilise l'enum
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction; // ⭐ RENOMMÉ depuis balanceAfter
    private String description;
    private String reference;
    private LocalDateTime createdAt;
}