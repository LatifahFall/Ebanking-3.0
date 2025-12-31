// ==================== NotificationAuditRepository.java ====================
package com.ebanking.notificationservice.repository;

import com.ebanking.notificationservice.model.NotificationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationAuditRepository extends JpaRepository<NotificationAudit, Long> {

    /**
     * Trouve tous les audits d'un utilisateur
     */
    List<NotificationAudit> findByUserIdOrderByTimestampDesc(String userId);

    /**
     * Compte les audits réussis
     */
    long countBySuccessTrue();

    /**
     * Trouve les audits par notification ID
     */
    List<NotificationAudit> findByNotificationId(Long notificationId);

    /**
     * Supprime les audits avant une certaine date (rétention RGPD)
     */
    long deleteByTimestampBefore(LocalDateTime cutoffDate);

    /**
     * Trouve les audits par statut
     */
    List<NotificationAudit> findByStatus(String status);
}