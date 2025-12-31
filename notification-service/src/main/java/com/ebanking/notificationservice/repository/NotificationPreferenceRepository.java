// ==================== NotificationPreferenceRepository.java ====================
package com.ebanking.notificationservice.repository;

import com.ebanking.notificationservice.model.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    /**
     * Trouve les préférences par userId
     */
    Optional<NotificationPreference> findByUserId(String userId);

    /**
     * Vérifie si un utilisateur a des préférences
     */
    boolean existsByUserId(String userId);

    /**
     * Supprime les préférences d'un utilisateur (pour RGPD - droit à l'oubli)
     */
    void deleteByUserId(String userId);
}

