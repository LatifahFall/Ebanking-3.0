package com.banking.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class CategoryBreakdown {
        private String category;
        private BigDecimal amount;
        private Integer count;
        private Double percentage;
    }

