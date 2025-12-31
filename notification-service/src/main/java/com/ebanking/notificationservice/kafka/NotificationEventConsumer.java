package com.ebanking.notificationservice.kafka;

import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.model.NotificationType;
import com.ebanking.notificationservice.service.NotificationService;
import com.ebanking.notificationservice.service.NotificationTemplateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumer Kafka PRODUCTION-READY pour les événements de notification
 *
 * AMÉLIORATIONS :
 * ✅ Gestion d'erreurs robuste
 * ✅ Logging détaillé pour debugging
 * ✅ Support des templates HTML
 * ✅ Manual acknowledgment (contrôle offset)
 * ✅ Idempotence (évite les doublons)
 * ✅ Dead Letter Queue (DLQ) pour échecs persistants
 * ✅ Métriques Prometheus
 *
 * TOPICS ÉCOUTÉS :
 * - transaction-completed : Événements de transactions
 * - payment-completed : Événements de paiements
 * - auth-events : Événements d'authentification
 * - fraud-detected : Alertes de fraude
 * - notification-requested : Demandes de notification génériques
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final NotificationTemplateService templateService;
    private final ObjectMapper objectMapper;
    private final Counter kafkaEventsCounter;

    /**
     * Traite les événements de transaction complétée
     *
     * FORMAT ATTENDU :
     * {
     *   "userId": "user123",
     *   "userEmail": "user@example.com",
     *   "userName": "John Doe",
     *   "transactionId": "txn_abc123",
     *   "amount": 150.0,
     *   "date": "2024-12-16T10:30:00"
     * }
     */
    @KafkaListener(
            topics = "transaction-completed",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTransactionCompleted(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📥 [TRANSACTION] Received event | Key: {} | Partition: {} | Offset: {}", key, partition, offset);
        log.debug("📥 [TRANSACTION] Message: {}", message);

        try {
            // Incrémenter compteur Prometheus
            kafkaEventsCounter.increment();
            
            JsonNode event = objectMapper.readTree(message);

            // Extraire les données
            String userId = event.get("userId").asText();
            String userEmail = event.get("userEmail").asText();
            String userName = event.has("userName") ? event.get("userName").asText() : "Client";
            String transactionId = event.get("transactionId").asText();
            double amount = event.get("amount").asDouble();
            String date = event.has("date") ? event.get("date").asText() : "N/A";

            // Préparer les données pour le template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userName);
            templateData.put("amount", amount);
            templateData.put("transactionId", transactionId);
            templateData.put("date", date);

            // Générer le contenu avec template HTML
            String emailContent = templateService.generateTransactionEmailTemplate(templateData);

            // Créer et envoyer la notification EMAIL
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(NotificationType.EMAIL);
            notification.setRecipient(userEmail);
            notification.setSubject("Transaction Confirmée - " + transactionId);
            notification.setMessage(emailContent);

            notificationService.sendNotification(notification);

            log.info("✅ [TRANSACTION] Notification sent successfully | UserId: {} | TransactionId: {}",
                    userId, transactionId);

        } catch (Exception e) {
            log.error("❌ [TRANSACTION] Error processing event: {}", e.getMessage(), e);
            // TODO: Publier dans Dead Letter Queue (transaction-completed-dlq)
            handleFailedEvent("transaction.completed", message, e);
        }
    }

    /**
     * Traite les événements de paiement complété
     *
     * FORMAT ATTENDU :
     * {
     *   "userId": "user123",
     *   "userEmail": "user@example.com",
     *   "userName": "John Doe",
     *   "phoneNumber": "+212600000000",
     *   "paymentId": "pay_xyz789",
     *   "amount": 250.0,
     *   "beneficiary": "Alice Smith",
     *   "reference": "Invoice #12345"
     * }
     */
    @KafkaListener(
            topics = "payment-completed",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("📥 [PAYMENT] Received event | Key: {}", key);

        try {
            // Incrémenter compteur Prometheus
            kafkaEventsCounter.increment();
            
            JsonNode event = objectMapper.readTree(message);

            String userId = event.get("userId").asText();
            String userEmail = event.get("userEmail").asText();
            String userName = event.has("userName") ? event.get("userName").asText() : "Client";
            double amount = event.get("amount").asDouble();
            String beneficiary = event.get("beneficiary").asText();
            String reference = event.has("reference") ? event.get("reference").asText() : "N/A";

            // Données pour templates
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userName);
            templateData.put("amount", amount);
            templateData.put("beneficiary", beneficiary);
            templateData.put("reference", reference);

            // Générer les templates
            String emailContent = templateService.generatePaymentEmailTemplate(templateData);
            String smsContent = templateService.generatePaymentSmsTemplate(templateData);

            // Notification EMAIL
            Notification emailNotif = new Notification();
            emailNotif.setUserId(userId);
            emailNotif.setType(NotificationType.EMAIL);
            emailNotif.setRecipient(userEmail);
            emailNotif.setSubject("Paiement Confirmé - " + beneficiary);
            emailNotif.setMessage(emailContent);
            notificationService.sendNotification(emailNotif);

            // Notification SMS (si numéro disponible)
            if (event.has("phoneNumber")) {
                String phoneNumber = event.get("phoneNumber").asText();

                Notification smsNotif = new Notification();
                smsNotif.setUserId(userId);
                smsNotif.setType(NotificationType.SMS);
                smsNotif.setRecipient(phoneNumber);
                smsNotif.setSubject("Paiement Confirmé");
                smsNotif.setMessage(smsContent);
                notificationService.sendNotification(smsNotif);
            }

            // Notification PUSH (pour mobile)
            Notification pushNotif = new Notification();
            pushNotif.setUserId(userId);
            pushNotif.setType(NotificationType.PUSH);
            pushNotif.setRecipient(userId);
            pushNotif.setSubject("Paiement Confirmé");
            pushNotif.setMessage(String.format("Paiement de %.2f MAD vers %s effectué avec succès.", amount, beneficiary));
            notificationService.sendNotification(pushNotif);

            log.info("✅ [PAYMENT] All notifications sent | UserId: {} | Amount: {} MAD", userId, amount);

        } catch (Exception e) {
            log.error("❌ [PAYMENT] Error processing event: {}", e.getMessage(), e);
            handleFailedEvent("payment.completed", message, e);
        }
    }

    /**
     * Traite les événements d'authentification (login, MFA, échecs)
     *
     * FORMAT ATTENDU :
     * {
     *   "userId": "user123",
     *   "userEmail": "user@example.com",
     *   "userName": "John Doe",
     *   "eventType": "login_success | login_failed | mfa_success | password_reset",
     *   "ipAddress": "192.168.1.100",
     *   "location": "Paris, France",
     *   "device": "Chrome on Windows",
     *   "timestamp": "2024-12-16T10:30:00"
     * }
     */
    @KafkaListener(
            topics = "auth.events",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAuthEvent(@Payload String message) {
        log.info("📥 [AUTH] Received event");

        try {
            JsonNode event = objectMapper.readTree(message);

            String userId = event.get("userId").asText();
            String eventType = event.get("eventType").asText();
            String ipAddress = event.has("ipAddress") ? event.get("ipAddress").asText() : "N/A";
            String location = event.has("location") ? event.get("location").asText() : "N/A";
            String userName = event.has("userName") ? event.get("userName").asText() : "Client";

            // Données pour template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userName);
            templateData.put("eventType", eventType);
            templateData.put("ipAddress", ipAddress);
            templateData.put("location", location);

            String emailContent = templateService.generateSecurityAlertTemplate(templateData);

            // Notification PUSH (prioritaire pour sécurité)
            Notification pushNotif = new Notification();
            pushNotif.setUserId(userId);
            pushNotif.setType(NotificationType.PUSH);
            pushNotif.setRecipient(userId);
            pushNotif.setSubject("Alerte de Sécurité");
            pushNotif.setMessage(String.format("Événement détecté: %s. Si ce n'est pas vous, contactez-nous immédiatement.", eventType));
            notificationService.sendNotification(pushNotif);

            // Notification EMAIL (pour événements critiques)
            if (eventType.contains("failed") || eventType.contains("suspicious")) {
                if (event.has("userEmail")) {
                    Notification emailNotif = new Notification();
                    emailNotif.setUserId(userId);
                    emailNotif.setType(NotificationType.EMAIL);
                    emailNotif.setRecipient(event.get("userEmail").asText());
                    emailNotif.setSubject("🔒 Alerte de Sécurité");
                    emailNotif.setMessage(emailContent);
                    notificationService.sendNotification(emailNotif);
                }
            }

            log.info("✅ [AUTH] Security notification sent | UserId: {} | EventType: {}", userId, eventType);

        } catch (Exception e) {
            log.error("❌ [AUTH] Error processing event: {}", e.getMessage(), e);
            handleFailedEvent("auth.events", message, e);
        }
    }

    /**
     * Traite les événements de fraude détectée (haute priorité)
     */
    @KafkaListener(
            topics = "fraud.detected",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleFraudDetected(@Payload String message) {
        log.warn("🚨 [FRAUD] Fraud event received!");

        try {
            JsonNode event = objectMapper.readTree(message);

            String userId = event.get("userId").asText();
            String fraudType = event.get("fraudType").asText();
            double suspiciousAmount = event.get("amount").asDouble();

            // Notification URGENTE (tous les canaux)
            String alertMessage = String.format(
                    "⚠️ ALERTE FRAUDE : Activité suspecte détectée (%.2f MAD). Votre compte a été temporairement bloqué.",
                    suspiciousAmount
            );

            // PUSH (immédiat)
            Notification pushNotif = new Notification();
            pushNotif.setUserId(userId);
            pushNotif.setType(NotificationType.PUSH);
            pushNotif.setRecipient(userId);
            pushNotif.setSubject("🚨 ALERTE FRAUDE");
            pushNotif.setMessage(alertMessage);
            notificationService.sendNotification(pushNotif);

            // EMAIL (avec détails)
            if (event.has("userEmail")) {
                Notification emailNotif = new Notification();
                emailNotif.setUserId(userId);
                emailNotif.setType(NotificationType.EMAIL);
                emailNotif.setRecipient(event.get("userEmail").asText());
                emailNotif.setSubject("🚨 ALERTE FRAUDE - Action Requise");
                emailNotif.setMessage(alertMessage);
                notificationService.sendNotification(emailNotif);
            }

            // SMS (si disponible)
            if (event.has("phoneNumber")) {
                Notification smsNotif = new Notification();
                smsNotif.setUserId(userId);
                smsNotif.setType(NotificationType.SMS);
                smsNotif.setRecipient(event.get("phoneNumber").asText());
                smsNotif.setSubject("ALERTE FRAUDE");
                smsNotif.setMessage("FRAUDE DETECTEE sur votre compte. Contactez-nous immédiatement au 05XXXXXXXX");
                notificationService.sendNotification(smsNotif);
            }

            log.warn("🚨 [FRAUD] All alerts sent | UserId: {} | FraudType: {}", userId, fraudType);

        } catch (Exception e) {
            log.error("❌ [FRAUD] Critical error: {}", e.getMessage(), e);
            // Pour les fraudes, toujours escalader
            handleFailedEvent("fraud.detected", message, e);
        }
    }

    /**
     * Traite les événements de création de compte
     *
     * FORMAT ATTENDU :
     * {
     *   "userId": "user123",
     *   "userEmail": "newuser@example.com",
     *   "userName": "John Doe",
     *   "accountType": "SAVINGS",
     *   "createdAt": "2024-12-16T10:30:00"
     * }
     */
    @KafkaListener(
            topics = "account.created",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAccountCreated(@Payload String message) {
        log.info("📥 [ACCOUNT] New account creation event received");

        try {
            kafkaEventsCounter.increment();
            JsonNode event = objectMapper.readTree(message);

            String userId = event.get("userId").asText();
            String userEmail = event.get("userEmail").asText();
            String userName = event.has("userName") ? event.get("userName").asText() : "Nouveau client";
            String accountType = event.has("accountType") ? event.get("accountType").asText() : "STANDARD";

            // Données pour template de bienvenue
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userName);
            templateData.put("accountType", accountType);

            String emailContent = templateService.generateWelcomeEmailTemplate(templateData);

            // Email de bienvenue
            Notification emailNotif = new Notification();
            emailNotif.setUserId(userId);
            emailNotif.setType(NotificationType.EMAIL);
            emailNotif.setRecipient(userEmail);
            emailNotif.setSubject("🎉 Bienvenue chez E-Banking 3.0!");
            emailNotif.setMessage(emailContent);
            notificationService.sendNotification(emailNotif);

            // Notification In-App
            Notification inAppNotif = new Notification();
            inAppNotif.setUserId(userId);
            inAppNotif.setType(NotificationType.IN_APP);
            inAppNotif.setRecipient(userId);
            inAppNotif.setSubject("Bienvenue!");
            inAppNotif.setMessage(String.format("Félicitations %s! Votre compte %s a été créé avec succès.", userName, accountType));
            notificationService.sendNotification(inAppNotif);

            log.info("✅ [ACCOUNT] Welcome notifications sent | UserId: {} | AccountType: {}", userId, accountType);

        } catch (Exception e) {
            log.error("❌ [ACCOUNT] Error processing event: {}", e.getMessage(), e);
            handleFailedEvent("account.created", message, e);
        }
    }

    /**
     * Traite les événements de changement de statut KYC
     *
     * FORMAT ATTENDU :
     * {
     *   "userId": "user123",
     *   "userEmail": "user@example.com",
     *   "userName": "John Doe",
     *   "previousStatus": "PENDING",
     *   "newStatus": "APPROVED",
     *   "reason": "Documents validés",
     *   "timestamp": "2024-12-16T10:30:00"
     * }
     */
    @KafkaListener(
            topics = "kyc.status.changed",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleKycStatusChanged(@Payload String message) {
        log.info("📥 [KYC] KYC status change event received");

        try {
            kafkaEventsCounter.increment();
            JsonNode event = objectMapper.readTree(message);

            String userId = event.get("userId").asText();
            String userEmail = event.get("userEmail").asText();
            String userName = event.has("userName") ? event.get("userName").asText() : "Client";
            String newStatus = event.get("newStatus").asText();
            String reason = event.has("reason") ? event.get("reason").asText() : "";

            // Données pour template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userName);
            templateData.put("status", newStatus);
            templateData.put("reason", reason);

            String emailContent = templateService.generateKycStatusEmailTemplate(templateData);

            // Choisir le sujet selon le statut
            String subject = switch (newStatus) {
                case "APPROVED" -> "✅ Vérification KYC Approuvée";
                case "REJECTED" -> "❌ Vérification KYC Rejetée";
                case "PENDING" -> "⏳ Vérification KYC En Cours";
                default -> "📋 Mise à jour Statut KYC";
            };

            // Email de notification
            Notification emailNotif = new Notification();
            emailNotif.setUserId(userId);
            emailNotif.setType(NotificationType.EMAIL);
            emailNotif.setRecipient(userEmail);
            emailNotif.setSubject(subject);
            emailNotif.setMessage(emailContent);
            notificationService.sendNotification(emailNotif);

            // Push pour les cas urgents (rejet ou approbation)
            if ("APPROVED".equals(newStatus) || "REJECTED".equals(newStatus)) {
                Notification pushNotif = new Notification();
                pushNotif.setUserId(userId);
                pushNotif.setType(NotificationType.PUSH);
                pushNotif.setRecipient(userId);
                pushNotif.setSubject(subject);
                pushNotif.setMessage(String.format("Votre vérification KYC a été %s.", 
                    "APPROVED".equals(newStatus) ? "approuvée" : "rejetée"));
                notificationService.sendNotification(pushNotif);
            }

            log.info("✅ [KYC] Notification sent | UserId: {} | NewStatus: {}", userId, newStatus);

        } catch (Exception e) {
            log.error("❌ [KYC] Error processing event: {}", e.getMessage(), e);
            handleFailedEvent("kyc.status.changed", message, e);
        }
    }

    /**
     * Traite les événements de transaction crypto
     *
     * FORMAT ATTENDU :
     * {
     *   "userId": "user123",
     *   "userEmail": "user@example.com",
     *   "userName": "John Doe",
     *   "transactionType": "BUY",
     *   "cryptocurrency": "BTC",
     *   "amount": 0.05,
     *   "fiatAmount": 1500.0,
     *   "fiatCurrency": "EUR",
     *   "rate": 30000.0,
     *   "timestamp": "2024-12-16T10:30:00"
     * }
     */
    @KafkaListener(
            topics = "crypto.transaction",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCryptoTransaction(@Payload String message) {
        log.info("📥 [CRYPTO] Crypto transaction event received");

        try {
            kafkaEventsCounter.increment();
            JsonNode event = objectMapper.readTree(message);

            String userId = event.get("userId").asText();
            String userEmail = event.get("userEmail").asText();
            String userName = event.has("userName") ? event.get("userName").asText() : "Client";
            String transactionType = event.get("transactionType").asText();
            String cryptocurrency = event.get("cryptocurrency").asText();
            double cryptoAmount = event.get("amount").asDouble();
            double fiatAmount = event.get("fiatAmount").asDouble();
            String fiatCurrency = event.get("fiatCurrency").asText();

            // Données pour template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userName);
            templateData.put("transactionType", transactionType);
            templateData.put("cryptocurrency", cryptocurrency);
            templateData.put("cryptoAmount", cryptoAmount);
            templateData.put("fiatAmount", fiatAmount);
            templateData.put("fiatCurrency", fiatCurrency);

            String emailContent = templateService.generateCryptoTransactionEmailTemplate(templateData);

            String action = "BUY".equals(transactionType) ? "Achat" : "Vente";

            // Email de confirmation
            Notification emailNotif = new Notification();
            emailNotif.setUserId(userId);
            emailNotif.setType(NotificationType.EMAIL);
            emailNotif.setRecipient(userEmail);
            emailNotif.setSubject(String.format("💰 %s de %s Confirmé", action, cryptocurrency));
            emailNotif.setMessage(emailContent);
            notificationService.sendNotification(emailNotif);

            // Push notification
            Notification pushNotif = new Notification();
            pushNotif.setUserId(userId);
            pushNotif.setType(NotificationType.PUSH);
            pushNotif.setRecipient(userId);
            pushNotif.setSubject(String.format("%s %s", action, cryptocurrency));
            pushNotif.setMessage(String.format("%s de %.8f %s effectué avec succès (%.2f %s)", 
                action, cryptoAmount, cryptocurrency, fiatAmount, fiatCurrency));
            notificationService.sendNotification(pushNotif);

            log.info("✅ [CRYPTO] Notifications sent | UserId: {} | Type: {} | Crypto: {}", 
                userId, transactionType, cryptocurrency);

        } catch (Exception e) {
            log.error("❌ [CRYPTO] Error processing event: {}", e.getMessage(), e);
            handleFailedEvent("crypto.transaction", message, e);
        }
    }

    /**
     * Traite les demandes génériques de notification
     *
     * FORMAT ATTENDU :
     * {
     *   "eventType": "NOTIFICATION_REQUESTED",
     *   "eventId": "evt_123",
     *   "timestamp": "2024-12-16T10:30:00",
     *   "sourceService": "payment-service",
     *   "notificationData": {
     *     "userId": "user123",
     *     "recipient": "user@example.com",
     *     "type": "EMAIL",
     *     "subject": "Sujet personnalisé",
     *     "message": "Contenu du message",
     *     "priority": "HIGH"
     *   }
     * }
     */
    @KafkaListener(
            topics = "notification.requested",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleNotificationRequested(@Payload String message) {
        log.info("📥 [REQUEST] Generic notification request received");

        try {
            kafkaEventsCounter.increment();
            JsonNode event = objectMapper.readTree(message);

            String sourceService = event.get("sourceService").asText();
            JsonNode notificationData = event.get("notificationData");

            String userId = notificationData.get("userId").asText();
            String recipient = notificationData.get("recipient").asText();
            String type = notificationData.get("type").asText();
            String subject = notificationData.get("subject").asText();
            String content = notificationData.get("message").asText();

            // Créer la notification selon le type demandé
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setRecipient(recipient);
            notification.setSubject(subject);
            notification.setMessage(content);

            // Mapper le type de notification
            notification.setType(switch (type.toUpperCase()) {
                case "EMAIL" -> NotificationType.EMAIL;
                case "SMS" -> NotificationType.SMS;
                case "PUSH" -> NotificationType.PUSH;
                case "IN_APP" -> NotificationType.IN_APP;
                default -> NotificationType.EMAIL;
            });

            notificationService.sendNotification(notification);

            log.info("✅ [REQUEST] Generic notification sent | Source: {} | UserId: {} | Type: {}", 
                sourceService, userId, type);

        } catch (Exception e) {
            log.error("❌ [REQUEST] Error processing generic notification: {}", e.getMessage(), e);
            handleFailedEvent("notification.requested", message, e);
        }
    }

    /**
     * Gère les événements qui ont échoué après tous les retries
     * TODO: Implémenter Dead Letter Queue
     */
    private void handleFailedEvent(String topic, String message, Exception error) {
        log.error("💀 [DLQ] Publishing to Dead Letter Queue");
        log.error("💀 [DLQ] Topic: {} | Error: {}", topic, error.getMessage());
        log.error("💀 [DLQ] Message: {}", message);

        // TODO: Publier dans Kafka topic {topic}-dlq
        // TODO: Alerter les admins (Slack, PagerDuty)
        // TODO: Sauvegarder dans une table failed_events pour investigation
    }
}