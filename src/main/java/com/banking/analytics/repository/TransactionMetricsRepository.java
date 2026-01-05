package com.banking.analytics.repository;

import com.banking.analytics.model.TransactionMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionMetricsRepository extends JpaRepository<TransactionMetrics, Long> {
}