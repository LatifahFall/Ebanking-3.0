package com.ebanking.notificationservice.service;

import com.ebanking.notificationservice.model.NotificationPreference;
import com.ebanking.notificationservice.model.NotificationType;
import com.ebanking.notificationservice.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Service de gestion des préférences de notification
 *
 * FONCTIONNALITÉS :
 * - Gestion des préférences par canal (Email, SMS, Push, In-App)
 * - Gestion des préférences par catégorie (Transaction, Payment, Security, Marketing)
 * - Mode "Do Not Disturb" avec plages horaires
 * - Vérification avant envoi de notification
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    /**
     * Récupère les préférences d'un utilisateur (ou crée des préférences par défaut)
     */
    public NotificationPreference getUserPreferences(String userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    /**
     * Crée les préférences par défaut pour un nouvel utilisateur
     */
    @Transactional
    public NotificationPreference createDefaultPreferences(String userId) {
        log.info("Creating default preferences for user: {}", userId);

        NotificationPreference prefs = new NotificationPreference();
        prefs.setUserId(userId);

        // Par défaut, tous les canaux sont activés
        prefs.setEmailEnabled(true);
        prefs.setSmsEnabled(true);
        prefs.setPushEnabled(true);
        prefs.setInAppEnabled(true);

        // Par défaut, toutes les notifications importantes sont activées
        prefs.setTransactionNotifications(true);
        prefs.setPaymentNotifications(true);
        prefs.setSecurityAlerts(true);
        prefs.setMarketingNotifications(false); // Marketing désactivé par défaut

        // Pas de Do Not Disturb par défaut
        prefs.setDoNotDisturb(false);

        return preferenceRepository.save(prefs);
    }

    /**
     * Met à jour les préférences d'un utilisateur
     */
    @Transactional
    public NotificationPreference updatePreferences(String userId, NotificationPreference newPreferences) {
        log.info("Updating preferences for user: {}", userId);

        NotificationPreference existingPrefs = getUserPreferences(userId);

        // Mise à jour des canaux
        existingPrefs.setEmailEnabled(newPreferences.isEmailEnabled());
        existingPrefs.setSmsEnabled(newPreferences.isSmsEnabled());
        existingPrefs.setPushEnabled(newPreferences.isPushEnabled());
        existingPrefs.setInAppEnabled(newPreferences.isInAppEnabled());

        // Mise à jour des catégories
        existingPrefs.setTransactionNotifications(newPreferences.isTransactionNotifications());
        existingPrefs.setPaymentNotifications(newPreferences.isPaymentNotifications());
        existingPrefs.setSecurityAlerts(newPreferences.isSecurityAlerts());
        existingPrefs.setMarketingNotifications(newPreferences.isMarketingNotifications());

        // Mise à jour Do Not Disturb
        existingPrefs.setDoNotDisturb(newPreferences.isDoNotDisturb());
        existingPrefs.setDoNotDisturbStart(newPreferences.getDoNotDisturbStart());
        existingPrefs.setDoNotDisturbEnd(newPreferences.getDoNotDisturbEnd());

        return preferenceRepository.save(existingPrefs);
    }

    /**
     * Active ou désactive toutes les notifications pour un utilisateur
     */
    @Transactional
    public NotificationPreference toggleAllNotifications(String userId, boolean enabled) {
        log.info("Toggling all notifications for user {}: {}", userId, enabled);

        NotificationPreference prefs = getUserPreferences(userId);

        prefs.setEmailEnabled(enabled);
        prefs.setSmsEnabled(enabled);
        prefs.setPushEnabled(enabled);
        prefs.setInAppEnabled(enabled);

        if (enabled) {
            // Si on active tout, activer aussi toutes les catégories sauf marketing
            prefs.setTransactionNotifications(true);
            prefs.setPaymentNotifications(true);
            prefs.setSecurityAlerts(true);
        } else {
            // Si on désactive tout, désactiver toutes les catégories
            prefs.setTransactionNotifications(false);
            prefs.setPaymentNotifications(false);
            prefs.setSecurityAlerts(false);
            prefs.setMarketingNotifications(false);
        }

        return preferenceRepository.save(prefs);
    }

    /**
     * Active ou désactive le mode Do Not Disturb
     */
    @Transactional
    public NotificationPreference toggleDoNotDisturb(String userId, boolean enabled) {
        log.info("Toggling Do Not Disturb for user {}: {}", userId, enabled);

        NotificationPreference prefs = getUserPreferences(userId);
        prefs.setDoNotDisturb(enabled);

        // Si activé pour la première fois, définir des horaires par défaut (22h-8h)
        if (enabled && prefs.getDoNotDisturbStart() == null) {
            prefs.setDoNotDisturbStart("22:00");
            prefs.setDoNotDisturbEnd("08:00");
        }

        return preferenceRepository.save(prefs);
    }

    /**
     * Vérifie si une notification doit être envoyée selon les préférences utilisateur
     *
     * @param userId ID de l'utilisateur
     * @param type Type de notification (EMAIL, SMS, PUSH, IN_APP)
     * @param category Catégorie (transaction, payment, security, marketing)
     * @return true si la notification peut être envoyée, false sinon
     */
    public boolean shouldSendNotification(String userId, NotificationType type, String category) {
        NotificationPreference prefs = getUserPreferences(userId);

        // 1. Vérifier si le canal est activé
        boolean channelEnabled = isChannelEnabled(prefs, type);
        if (!channelEnabled) {
            log.debug("Channel {} disabled for user {}", type, userId);
            return false;
        }

        // 2. Vérifier si la catégorie est activée
        boolean categoryEnabled = isCategoryEnabled(prefs, category);
        if (!categoryEnabled) {
            log.debug("Category {} disabled for user {}", category, userId);
            return false;
        }

        // 3. Vérifier le mode Do Not Disturb
        // EXCEPTION : Les alertes de sécurité passent toujours
        if ("security".equals(category)) {
            log.debug("Security alert - bypassing Do Not Disturb");
            return true;
        }

        boolean inDoNotDisturbPeriod = isInDoNotDisturbPeriod(prefs);
        if (inDoNotDisturbPeriod) {
            log.debug("User {} is in Do Not Disturb period", userId);
            return false;
        }

        return true;
    }

    /**
     * Vérifie si le canal de notification est activé
     */
    private boolean isChannelEnabled(NotificationPreference prefs, NotificationType type) {
        return switch (type) {
            case EMAIL -> prefs.isEmailEnabled();
            case SMS -> prefs.isSmsEnabled();
            case PUSH -> prefs.isPushEnabled();
            case IN_APP -> prefs.isInAppEnabled();
        };
    }

    /**
     * Vérifie si la catégorie de notification est activée
     */
    private boolean isCategoryEnabled(NotificationPreference prefs, String category) {
        if (category == null) {
            return true; // Si pas de catégorie spécifiée, on envoie
        }

        return switch (category.toLowerCase()) {
            case "transaction" -> prefs.isTransactionNotifications();
            case "payment" -> prefs.isPaymentNotifications();
            case "security" -> prefs.isSecurityAlerts();
            case "marketing" -> prefs.isMarketingNotifications();
            default -> true; // Catégorie inconnue = on envoie
        };
    }

    /**
     * Vérifie si on est dans la période Do Not Disturb
     */
    private boolean isInDoNotDisturbPeriod(NotificationPreference prefs) {
        if (!prefs.isDoNotDisturb()) {
            return false;
        }

        if (prefs.getDoNotDisturbStart() == null || prefs.getDoNotDisturbEnd() == null) {
            return false;
        }

        try {
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.parse(prefs.getDoNotDisturbStart(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime end = LocalTime.parse(prefs.getDoNotDisturbEnd(), DateTimeFormatter.ofPattern("HH:mm"));

            // Cas 1 : Période dans la même journée (ex: 10:00 - 18:00)
            if (start.isBefore(end)) {
                return now.isAfter(start) && now.isBefore(end);
            }
            // Cas 2 : Période traversant minuit (ex: 22:00 - 08:00)
            else {
                return now.isAfter(start) || now.isBefore(end);
            }

        } catch (Exception e) {
            log.error("Error parsing Do Not Disturb times for user {}: {}",
                    prefs.getUserId(), e.getMessage());
            return false; // En cas d'erreur, on n'applique pas le DND
        }
    }

    /**
     * Supprime les préférences d'un utilisateur (RGPD - droit à l'oubli)
     */
    @Transactional
    public void deleteUserPreferences(String userId) {
        log.info("Deleting preferences for user: {} (GDPR compliance)", userId);
        preferenceRepository.deleteByUserId(userId);
    }

    /**
     * Vérifie si un utilisateur a des préférences configurées
     */
    public boolean hasPreferences(String userId) {
        return preferenceRepository.existsByUserId(userId);
    }
}