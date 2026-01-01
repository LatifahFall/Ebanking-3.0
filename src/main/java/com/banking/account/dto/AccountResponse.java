// ============================================================================
// AccountResponse.java – DTO de réponse pour un compte bancaire
// Version finale, 100% alignée avec Account entity et besoins frontend / API
// ============================================================================
package com.banking.account.dto;

import com.banking.account.model.Account;
import com.banking.account.model.Account.AccountStatus;
import com.banking.account.model.Account.AccountType;
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
public class AccountResponse {

    /**
     * ID interne du compte (clé primaire)
     */
    private Long id;

    /**
     * Numéro de compte visible par l'utilisateur (ex: ACC-1700000000-123)
     */
    private String accountNumber;

    /**
     * ID de l'utilisateur propriétaire du compte
     */
    private Long userId;

    /**
     * Type de compte : CHECKING, SAVINGS, etc.
     */
    private AccountType accountType;

    /**
     * Devise du compte (EUR, USD, MAD, etc.)
     */
    private String currency;

    /**
     * Solde actuel du compte
     */
    private BigDecimal balance;

    /**
     * Statut actuel du compte : ACTIVE, SUSPENDED, CLOSED
     */
    private AccountStatus status;

    /**
     * Date de création du compte
     */
    private LocalDateTime createdAt;

    /**
     * Date de dernière mise à jour du compte
     */
    private LocalDateTime updatedAt;

    /**
     * Date de suspension (null si jamais suspendu ou si réactivé)
     */
    private LocalDateTime suspendedAt;

    /**
     * Date de clôture définitive du compte (null si toujours ouvert)
     */
    private LocalDateTime closedAt;

    /**
     * Raison de la suspension (null si pas suspendu)
     * Exemples : "FRAUD_DETECTED: Transaction inhabituelle...", "MANUAL_REVIEW", etc.
     */
    private String suspensionReason;

    /**
     * Raison de la clôture (null si pas fermé)
     * Exemples : "CLIENT_REQUEST", "INACTIVITY", etc.
     */
    private String closureReason;

    /**
     * Solde final au moment de la clôture (pour audit)
     * Doit être zéro pour permettre la clôture
     */
    private BigDecimal finalBalance;
}