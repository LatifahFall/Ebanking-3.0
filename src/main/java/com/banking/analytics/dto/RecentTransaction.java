package com.banking.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentTransaction {
    private String transactionId;
    private String type;
    private BigDecimal amount;
    private String merchant;
    private LocalDateTime date;
}