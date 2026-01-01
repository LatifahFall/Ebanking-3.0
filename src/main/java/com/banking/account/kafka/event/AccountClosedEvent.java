// AccountClosedEvent - CORRIGÉ
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
public class AccountClosedEvent {
    private Long accountId;
    private String closureReason;
    private BigDecimal finalBalance; // ⭐ AJOUTÉ
    private LocalDateTime timestamp; // ⭐ AJOUTÉ (optionnel)
}
