// ==================== 9. UpdateAccountRequest (Champ manquant) ====================
package com.banking.account.dto;

import com.banking.account.model.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {
    private Account.AccountStatus status;
    private Account.AccountType accountType; // ⭐ AJOUTÉ
    private String currency;
}
