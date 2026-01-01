// ============================================================================
// Account.java – Entity finale, production-ready & fully audited
// ============================================================================
package com.banking.account.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "accounts",
        indexes = {
                @Index(name = "idx_account_number", columnList = "accountNumber", unique = true),
                @Index(name = "idx_user_id", columnList = "userId"),
                @Index(name = "idx_status", columnList = "status")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 34)
    private String accountNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "EUR";

    @Column(name = "balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    // ==================== Suspension fields ====================
    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;

    @Column(name = "suspended_by", length = 100)
    private String suspendedBy;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    // ==================== Closure fields ====================
    @Column(name = "closure_reason", length = 500)
    private String closureReason;

    @Column(name = "closed_by", length = 100)
    private String closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Solde final au moment de la clôture (doit être zéro)
    @Column(name = "final_balance", precision = 19, scale = 4,nullable = true)
    private BigDecimal finalBalance;

    // ==================== Audit timestamps ====================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Enums ====================
    public enum AccountType {
        CHECKING,
        SAVINGS,
        BUSINESS
    }

    public enum AccountStatus {
        ACTIVE,
        SUSPENDED,
        CLOSED
    }

    // ==================== Business methods ====================

    public boolean isSuspended() {
        return this.status == AccountStatus.SUSPENDED;
    }

    public boolean isClosed() {
        return this.status == AccountStatus.CLOSED;
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    /**
     * Vérifie que le compte est opérationnel (ni suspendu ni fermé)
     * Utilisé avant tout débit/crédit ou opération sensible
     */
    public void assertOperational() {
        if (isClosed()) {
            throw new IllegalStateException("Account is closed and cannot be used for operations");
        }
        if (isSuspended()) {
            throw new IllegalStateException("Account is suspended and cannot be used for operations");
        }
    }

    /**
     * Crédite le compte d'un montant positif
     */
    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * Débite le compte (vérifie le solde disponible)
     */
    public void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance for debit. Current: " + this.balance + ", Requested: " + amount);
        }
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Méthode appelée juste avant clôture pour figer le solde final
     */
    public void freezeFinalBalance() {
        this.finalBalance = this.balance;
        if (this.finalBalance.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close account: non-zero balance. Current: " + this.finalBalance);        }
    }
}