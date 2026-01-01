package com.banking.account.kafka.event;

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
public class FraudDetectedEvent {

    /**
     * ID unique de l'événement de fraude
     */
    private UUID fraudId;

    /**
     * ID du paiement suspect (lien avec payment.completed)
     */
    private UUID paymentId;

    /**
     * ID du compte concerné
     */
    private UUID accountId;

    /**
     * ID de l'utilisateur (pour traçabilité et notifications)
     */
    private UUID userId;

    /**
     * Montant de la transaction suspecte
     */
    private BigDecimal amount;

    /**
     * Type de fraude détectée
     * Exemples : SUSPICIOUS_AMOUNT, UNUSUAL_PATTERN, BLACKLIST, etc.
     */
    private String fraudType;

    /**
     * Raison lisible de la détection
     * Exemple : "Transaction inhabituelle de 5000€ détectée"
     */
    private String reason;

    /**
     * Date et heure de détection de la fraude
     */
    private Instant detectedAt;

    /**
     * Action décidée par le système anti-fraude
     * BLOCKED → compte doit être suspendu immédiatement
     * PENDING_REVIEW → alerte seulement, pas de blocage auto
     */
    private String action; // "BLOCKED" ou "PENDING_REVIEW"
}