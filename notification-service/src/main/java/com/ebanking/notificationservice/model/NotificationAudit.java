// ==================== NotificationAudit.java ====================
package com.ebanking.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entité d'audit pour traçabilité des notifications (RGPD compliant)
 */
@Entity
@Table(name = "notification_audits", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long notificationId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String action; // NOTIFICATION_SENT, NOTIFICATION_FAILED, NOTIFICATION_READ

    @Column(nullable = false)
    private String status; // SENT, FAILED, READ

    @Column(columnDefinition = "TEXT")
    private String details; // JSON avec informations supplémentaires

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}