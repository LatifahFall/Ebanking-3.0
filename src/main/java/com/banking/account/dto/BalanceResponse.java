// ==================== 6. BalanceResponse (Champs manquants) ====================
package com.banking.account.dto;

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
public class BalanceResponse {
    private Long accountId;
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal availableBalance; // ⭐ AJOUTÉ
    private String currency;
    private LocalDateTime lastUpdated;
}

