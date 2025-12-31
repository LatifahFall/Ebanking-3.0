package com.ebanking.notificationservice.repository;

import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Trouve toutes les notifications d'un utilisateur
     */
    List<Notification> findByUserId(String userId);

    /**
     * Trouve les notifications par statut
     */
    List<Notification> findByStatus(String status);

    /**
     * Trouve les notifications d'un utilisateur par type
     * NOUVELLE MÉTHODE pour les notifications IN_APP
     */
    List<Notification> findByUserIdAndType(String userId, NotificationType type);

    /**
     * Trouve les notifications IN_APP non lues d'un utilisateur
     */
    List<Notification> findByUserIdAndTypeAndStatus(String userId, NotificationType type, String status);

    /**
     * Compte les notifications non lues d'un utilisateur
     */
    long countByUserIdAndStatus(String userId, String status);
}