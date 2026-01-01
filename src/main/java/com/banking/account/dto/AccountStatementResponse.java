// ==================== 8. AccountStatementResponse (Champs manquants) ====================
package com.banking.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementResponse {
    private Long accountId;
    private String accountNumber;
    private String currency; // ⭐ AJOUTÉ
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal openingBalance; // ⭐ AJOUTÉ
    private BigDecimal closingBalance; // ⭐ RENOMMÉ depuis currentBalance
    private List<TransactionResponse> transactions;
    private LocalDateTime generatedAt; // ⭐ AJOUTÉ
}
