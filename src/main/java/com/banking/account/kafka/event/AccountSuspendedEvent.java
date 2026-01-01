// AccountSuspendedEvent - CORRIGÉ
package com.banking.account.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSuspendedEvent {
    private Long accountId;
    private String suspensionReason; // ⭐ RENOMMÉ depuis reason
    private String suspendedBy;
    private LocalDateTime timestamp; // ⭐ AJOUTÉ (optionnel)
}