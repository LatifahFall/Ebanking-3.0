// ========== TransactionMetrics.java ==========
package com.banking.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transaction_metrics", indexes = {
        @Index(name = "idx_tx_date_type", columnList = "metric_date,transaction_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "transaction_type", length = 50)
    private String transactionType;

    @Column(name = "total_count")
    private Long totalCount;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "avg_amount", precision = 15, scale = 2)
    private BigDecimal avgAmount;

    @Column(name = "min_amount", precision = 15, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "success_count")
    private Long successCount;

    @Column(name = "failed_count")
    private Long failedCount;
}