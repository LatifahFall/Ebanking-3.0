package com.banking.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "spending_categories", indexes = {
        @Index(name = "idx_user_period_category", columnList = "user_id,period_start,category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "transaction_count")
    private Integer transactionCount;

    @Column(name = "percentage_of_total")
    private Double percentageOfTotal;
}