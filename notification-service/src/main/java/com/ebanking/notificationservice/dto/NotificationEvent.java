package com.ebanking.notificationservice.dto;

import com.ebanking.notificationservice.model.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les événements Kafka de notification
 * Utilisé pour :
 * - CONSUMER : recevoir des demandes de notification des autres services
 * - PRODUCER : publier le statut/résultat d'envoi de notification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    /**
     * Type d'événement
     */
    private EventType eventType;

    /**
     * ID unique de l'événement
     */
    private String eventId;

    /**
     * Timestamp de l'événement
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Service source qui a émis l'événement
     */
    private String sourceService;

    /**
     * Données de la notification
     */
    private NotificationData notificationData;

    /**
     * Résultat de l'envoi (pour les événements NOTIFICATION_SENT/FAILED)
     */
    private NotificationResult result;

    /**
     * Types d'événements Kafka
     */
    public enum EventType {
        // Événements entrants (CONSUMER)
        NOTIFICATION_REQUESTED,      // Demande d'envoi de notification
        TRANSACTION_COMPLETED,       // Transaction terminée → notifier
        PAYMENT_COMPLETED,           // Paiement terminé → notifier
        ACCOUNT_CREATED,             // Compte créé → email de bienvenue
        LOGIN_ATTEMPT,               // Tentative de connexion → alerte sécurité
        PASSWORD_RESET_REQUESTED,    // Réinitialisation mot de passe
        KYC_STATUS_CHANGED,          // Changement statut KYC
        CRYPTO_TRANSACTION,          // Transaction crypto → notification

        // Événements sortants (PRODUCER)
        NOTIFICATION_SENT,           // Notification envoyée avec succès
        NOTIFICATION_FAILED,         // Échec d'envoi
        NOTIFICATION_DELIVERED,      // Notification livrée (confirmation)
        NOTIFICATION_READ            // Notification lue par l'utilisateur
    }

    /**
     * Données de notification
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationData {
        private String userId;
        private String recipient;        // Email, numéro de téléphone, etc.
        private NotificationType type;   // EMAIL, SMS, PUSH, IN_APP
        private String subject;
        private String message;
        private String templateId;       // Optionnel: ID du template à utiliser
        private Object templateData;     // Données pour remplir le template
        private Priority priority;       // Priorité de la notification

        public enum Priority {
            LOW,
            NORMAL,
            HIGH,
            URGENT
        }
    }

    /**
     * Résultat d'envoi de notification
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResult {
        private Long notificationId;
        private String status;           // SENT, FAILED, DELIVERED
        private String errorMessage;
        private LocalDateTime sentAt;
        private String provider;         // EMAIL, TWILIO, FCM, etc.
    }
}
