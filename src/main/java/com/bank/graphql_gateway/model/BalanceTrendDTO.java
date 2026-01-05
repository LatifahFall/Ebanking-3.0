package com.bank.graphql_gateway.model;

import java.util.List;

public class BalanceTrendDTO {
    private String period;
    private List<DataPointDTO> dataPoints;

    // Constructors
    public BalanceTrendDTO() {}

    public BalanceTrendDTO(String period, List<DataPointDTO> dataPoints) {
        this.period = period;
        this.dataPoints = dataPoints;
    }

    // Getters and Setters
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public List<DataPointDTO> getDataPoints() { return dataPoints; }
    public void setDataPoints(List<DataPointDTO> dataPoints) { this.dataPoints = dataPoints; }
}
