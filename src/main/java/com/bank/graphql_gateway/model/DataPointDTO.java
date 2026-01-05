package com.bank.graphql_gateway.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DataPointDTO {
    private LocalDateTime timestamp;
    private BigDecimal value;

    // Constructors
    public DataPointDTO() {}

    public DataPointDTO(LocalDateTime timestamp, BigDecimal value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
}
