package com.bank.graphql_gateway.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecentTransactionDTO {
    private String transactionId;
    private String type;
    private BigDecimal amount;
    private String merchant;
    private LocalDateTime date;

    // Constructors
    public RecentTransactionDTO() {}

    public RecentTransactionDTO(String transactionId, String type, BigDecimal amount, 
                                String merchant, LocalDateTime date) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.merchant = merchant;
        this.date = date;
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
}
