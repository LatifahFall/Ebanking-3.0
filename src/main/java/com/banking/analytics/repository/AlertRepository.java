package com.banking.analytics.repository;

import com.banking.analytics.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {

    List<Alert> findByUserIdAndStatus(String userId, Alert.AlertStatus status);

    boolean existsByUserIdAndAlertTypeAndStatus(String userId, Alert.AlertType alertType,
                                                Alert.AlertStatus status);
}