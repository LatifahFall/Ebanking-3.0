package com.ebanking.notificationservice.controller;

import com.ebanking.notificationservice.dto.ApiResponse;
import com.ebanking.notificationservice.dto.BulkNotificationRequest;
import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.model.NotificationAudit;
import com.ebanking.notificationservice.model.NotificationPreference;
import com.ebanking.notificationservice.service.NotificationAuditService;
import com.ebanking.notificationservice.service.NotificationPreferenceService;
import com.ebanking.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST pour le service de notifications - Version Complète
 *
 * ENDPOINTS :
 * - POST /api/notifications : Envoyer une notification
 * - POST /api/notifications/bulk : Envoi en masse
 * - GET /api/notifications/user/{userId} : Historique utilisateur
 * - GET /api/notifications/in-app/{userId} : Notifications IN_APP
 * - PUT /api/notifications/{id}/read : Marquer comme lu
 * - GET /api/notifications/preferences/{userId} : Récupérer préférences
 * - PUT /api/notifications/preferences/{userId} : Modifier préférences
 * - PUT /api/notifications/preferences/{userId}/toggle-all : Activer/Désactiver tout
 * - PUT /api/notifications/preferences/{userId}/do-not-disturb : Mode silencieux
 * - GET /api/notifications/audit/{userId} : Historique d'audit
 * - GET /api/notifications/stats : Statistiques
 * - GET /api/notifications/pending : Notifications en attente
 * - GET /api/notifications/health : Health check
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // À ajuster en production
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;
    private final NotificationAuditService auditService;

    /**
     * Envoie une notification simple
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Notification>> sendNotification(@RequestBody Notification notification) {
        try {
            Notification sent = notificationService.sendNotification(notification);
            return ResponseEntity.ok(ApiResponse.success("Notification envoyée avec succès", sent));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors de l'envoi: " + e.getMessage()));
        }
    }

    /**
     * Envoie des notifications en masse (broadcast)
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<String>> sendBulkNotifications(@RequestBody BulkNotificationRequest request) {
        try {
            notificationService.sendBulkNotifications(
                    request.getUserIds(),
                    request.getType(),
                    request.getSubject(),
                    request.getMessage(),
                    request.getCategory()
            );
            return ResponseEntity.ok(ApiResponse.success(
                    "Envoi en masse démarré pour " + request.getUserIds().size() + " utilisateurs",
                    "PROCESSING"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors de l'envoi en masse: " + e.getMessage()));
        }
    }

    /**
     * Récupère l'historique des notifications d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications(@PathVariable String userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Récupère les notifications IN_APP non lues (centre de notifications)
     */
    @GetMapping("/in-app/{userId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getInAppNotifications(@PathVariable String userId) {
        List<Notification> notifications = notificationService.getInAppNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Marque une notification IN_APP comme lue
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok(ApiResponse.success("Notification marquée comme lue"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur: " + e.getMessage()));
        }
    }

    /**
     * Récupère les préférences de notification d'un utilisateur
     */
    @GetMapping("/preferences/{userId}")
    public ResponseEntity<ApiResponse<NotificationPreference>> getPreferences(@PathVariable String userId) {
        NotificationPreference prefs = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(prefs));
    }

    /**
     * Met à jour les préférences de notification
     */
    @PutMapping("/preferences/{userId}")
    public ResponseEntity<ApiResponse<NotificationPreference>> updatePreferences(
            @PathVariable String userId,
            @RequestBody NotificationPreference preferences) {

        NotificationPreference updated = preferenceService.updatePreferences(userId, preferences);
        return ResponseEntity.ok(ApiResponse.success("Préférences mises à jour", updated));
    }

    /**
     * Active/désactive toutes les notifications pour un utilisateur
     */
    @PutMapping("/preferences/{userId}/toggle-all")
    public ResponseEntity<ApiResponse<NotificationPreference>> toggleAllNotifications(
            @PathVariable String userId,
            @RequestParam boolean enabled) {

        NotificationPreference updated = preferenceService.toggleAllNotifications(userId, enabled);
        return ResponseEntity.ok(ApiResponse.success(
                enabled ? "Notifications activées" : "Notifications désactivées",
                updated
        ));
    }

    /**
     * Active/désactive le mode Do Not Disturb
     */
    @PutMapping("/preferences/{userId}/do-not-disturb")
    public ResponseEntity<ApiResponse<NotificationPreference>> toggleDoNotDisturb(
            @PathVariable String userId,
            @RequestParam boolean enabled) {

        NotificationPreference updated = preferenceService.toggleDoNotDisturb(userId, enabled);
        return ResponseEntity.ok(ApiResponse.success(
                enabled ? "Mode silencieux activé" : "Mode silencieux désactivé",
                updated
        ));
    }

    /**
     * Récupère l'historique d'audit d'un utilisateur
     */
    @GetMapping("/audit/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationAudit>>> getUserAuditHistory(@PathVariable String userId) {
        List<NotificationAudit> audits = auditService.getUserAuditHistory(userId);
        return ResponseEntity.ok(ApiResponse.success(audits));
    }

    /**
     * Récupère les statistiques globales de notifications
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = auditService.getAuditStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Récupère les notifications en attente (pour monitoring)
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Notification>>> getPendingNotifications() {
        List<Notification> pending = notificationService.getPendingNotifications();
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running! 🚀");
    }
}