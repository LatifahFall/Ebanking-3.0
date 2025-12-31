package com.ebanking.notificationservice.service;

import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.model.NotificationAudit;
import com.ebanking.notificationservice.repository.NotificationAuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service d'audit pour tracer toutes les notifications
 *
 * RÔLE : Conforme aux exigences RGPD et réglementations bancaires
 *
 * TRAÇABILITÉ :
 * - Qui a envoyé la notification
 * - À qui elle a été envoyée
 * - Quand et par quel canal
 * - Succès ou échec avec raison
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationAuditService {

    private final NotificationAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    /**
     * Enregistre un audit pour une notification envoyée
     */
    @Transactional
    public void logNotificationSent(Notification notification) {
        try {
            NotificationAudit audit = new NotificationAudit();
            audit.setNotificationId(notification.getId());
            audit.setUserId(notification.getUserId());
            audit.setType(notification.getType());
            audit.setRecipient(notification.getRecipient());
            audit.setAction("NOTIFICATION_SENT");
            audit.setStatus(notification.getStatus());
            audit.setTimestamp(LocalDateTime.now());

            Map<String, Object> details = new HashMap<>();
            details.put("subject", notification.getSubject());
            details.put("sentAt", notification.getSentAt());
            details.put("channel", notification.getType().toString());

            audit.setDetails(objectMapper.writeValueAsString(details));
            audit.setSuccess(notification.getStatus().equals("SENT"));

            auditRepository.save(audit);

            log.info("📝 Audit logged for notification ID: {}", notification.getId());

        } catch (Exception e) {
            log.error("❌ Failed to log audit for notification {}: {}",
                    notification.getId(), e.getMessage());
        }
    }

    /**
     * Enregistre un échec d'envoi de notification
     */
    @Transactional
    public void logNotificationFailure(Notification notification, String errorMessage) {
        try {
            NotificationAudit audit = new NotificationAudit();
            audit.setNotificationId(notification.getId());
            audit.setUserId(notification.getUserId());
            audit.setType(notification.getType());
            audit.setRecipient(notification.getRecipient());
            audit.setAction("NOTIFICATION_FAILED");
            audit.setStatus("FAILED");
            audit.setTimestamp(LocalDateTime.now());

            Map<String, Object> details = new HashMap<>();
            details.put("subject", notification.getSubject());
            details.put("error", errorMessage);
            details.put("channel", notification.getType().toString());

            audit.setDetails(objectMapper.writeValueAsString(details));
            audit.setSuccess(false);

            auditRepository.save(audit);

            log.info("📝 Failure audit logged for notification ID: {}", notification.getId());

        } catch (Exception e) {
            log.error("❌ Failed to log failure audit: {}", e.getMessage());
        }
    }

    /**
     * Récupère l'historique d'audit pour un utilisateur
     */
    public List<NotificationAudit> getUserAuditHistory(String userId) {
        return auditRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Récupère les statistiques d'audit
     */
    public Map<String, Object> getAuditStats() {
        long totalNotifications = auditRepository.count();
        long successfulNotifications = auditRepository.countBySuccessTrue();
        long failedNotifications = totalNotifications - successfulNotifications;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNotifications", totalNotifications);
        stats.put("successfulNotifications", successfulNotifications);
        stats.put("failedNotifications", failedNotifications);
        stats.put("successRate", totalNotifications > 0
                ? (double) successfulNotifications / totalNotifications * 100
                : 0.0);

        log.info("📊 Audit stats calculated: {} total, {} success, {} failed",
                totalNotifications, successfulNotifications, failedNotifications);

        return stats;
    }

    /**
     * Supprime les audits anciens (conformité RGPD - rétention limitée)
     */
    @Transactional
    public void cleanupOldAudits(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        long deletedCount = auditRepository.deleteByTimestampBefore(cutoffDate);

        log.info("🗑️ Cleaned up {} old audit logs (older than {} days)",
                deletedCount, retentionDays);
    }
}