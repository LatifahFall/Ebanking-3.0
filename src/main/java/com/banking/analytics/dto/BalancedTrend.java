package com.banking.analytics.dto;


import com.banking.analytics.dto.DataPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class BalancedTrend {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceTrend {  // ← added 'static'
        private String period;
        private List<DataPoint> dataPoints;
    }
}