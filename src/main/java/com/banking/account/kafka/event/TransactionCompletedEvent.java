// ============================================================================
// TransactionCompletedEvent.java – Compatible notification-service & audit-service
// ============================================================================
package com.banking.account.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompletedEvent {

    // Identifiants (OBLIGATOIRES pour traçabilité)
    private UUID transactionId;
    private UUID accountId;
    private UUID userId;           // ← Ajouté (pour notification-service)
    private String accountNumber;  // ← Ajouté (IBAN pour audit)

    // Montant & devise
    private BigDecimal amount;
    private String currency = "EUR";

    // Type de transaction (essentiel pour routing notifications)
    private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, FEE

    // Détails métier
    private String description;    // "Virement vers John Doe", "Retrait ATM", etc.
    private String reference;      // Référence unique (idempotence)
    private String recipientName;  // Bénéficiaire (pour notifications)
    private String recipientIban;  // IBAN destinataire (audit PSD2)

    // Soldes (pour analytics + notifications)
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;

    // Métadonnées techniques
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant completedAt = Instant.now();

    private String status = "SUCCESS"; // SUCCESS, PARTIAL, FAILED (avec retry)

    // Champs optionnels (fraude / géoloc)
    private String ipAddress;
    private String userAgent;
    private String geolocation;    // "Paris, France" (anonymisé)

    // Compliance (pour audit-service)
    private boolean sensitive = true;
    
    // Versioning (évolutivité)
    private int version = 1;
}
