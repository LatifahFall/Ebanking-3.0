// ========== Repositories ==========
package com.banking.analytics.repository;

import com.banking.analytics.model.Alert;
import com.banking.analytics.model.UserMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMetricsRepository extends JpaRepository<UserMetrics, Long> {

    Optional<UserMetrics> findByUserIdAndMetricDate(String userId, LocalDate metricDate);

    List<UserMetrics> findByUserIdAndMetricDateBetween(String userId, LocalDate start, LocalDate end);

    @Query("SELECT COUNT(DISTINCT um.userId) FROM UserMetrics um WHERE um.metricDate = :date")
    long countDistinctUserIds(LocalDate date);
}

