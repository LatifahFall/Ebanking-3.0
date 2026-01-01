// ============================================================================
// PaymentCompletedEvent.java
// Structure EXACTE requise par le topic payment.completed du payment-service
// ============================================================================
package com.banking.account.kafka.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PaymentCompletedEvent extends BaseEvent {

    /**
     * ID unique du paiement (généré par payment-service)
     */
    private UUID paymentId;

    /**
     * ID du compte impacté (cohérent avec Account.id qui est Long dans ton modèle)
     */
    private Long accountId;

    /**
     * Montant de la transaction
     */
    private BigDecimal amount;

    /**
     * Devise (EUR, USD, MAD, etc.)
     */
    private String currency;

    /**
     * Type de transaction : PAYMENT | TRANSFER | WITHDRAWAL
     */
    private String transactionType;

    /**
     * Statut fixe pour cet événement
     */
    @Builder.Default
    private String status = "COMPLETED";

    /**
     * Date et heure de finalisation du paiement
     */
    private Instant completedAt;

    /**
     * Métadonnées libres définies par payment-service
     * Doit contenir au minimum :
     * - "description": string
     * - "merchantId": string (optionnel selon le type de paiement)
     */
    private Map<String, String> metadata;

    /**
     * Type d'événement pour routing ou filtrage (optionnel mais utile)
     */
    @Builder.Default
    private String eventType = "PAYMENT_COMPLETED";
}