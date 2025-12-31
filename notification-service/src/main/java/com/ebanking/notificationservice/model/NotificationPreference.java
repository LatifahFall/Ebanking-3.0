// ==================== NotificationPreference.java ====================
package com.ebanking.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entité représentant les préférences de notification d'un utilisateur
 */
@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    // Canaux de notification
    @Column(nullable = false)
    private boolean emailEnabled = true;

    @Column(nullable = false)
    private boolean smsEnabled = true;

    @Column(nullable = false)
    private boolean pushEnabled = true;

    @Column(nullable = false)
    private boolean inAppEnabled = true;

    // Types de notifications
    @Column(nullable = false)
    private boolean transactionNotifications = true;

    @Column(nullable = false)
    private boolean paymentNotifications = true;

    @Column(nullable = false)
    private boolean securityAlerts = true;

    @Column(nullable = false)
    private boolean marketingNotifications = false;

    // Do Not Disturb
    @Column(nullable = false)
    private boolean doNotDisturb = false;

    private String doNotDisturbStart; // Format: "22:00"
    private String doNotDisturbEnd;   // Format: "08:00"

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

