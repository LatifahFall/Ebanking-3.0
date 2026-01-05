package com.bank.graphql_gateway.model;

import java.math.BigDecimal;

public class CategoryBreakdownDTO {
    private String category;
    private BigDecimal amount;
    private Integer count;
    private Double percentage;

    // Constructors
    public CategoryBreakdownDTO() {}

    public CategoryBreakdownDTO(String category, BigDecimal amount, Integer count, Double percentage) {
        this.category = category;
        this.amount = amount;
        this.count = count;
        this.percentage = percentage;
    }

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
}
