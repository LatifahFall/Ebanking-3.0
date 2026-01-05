package com.bank.graphql_gateway.model;

import java.math.BigDecimal;

public class BuyCryptoInput {
    private String symbol;
    private BigDecimal eurAmount;

    public BuyCryptoInput() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getEurAmount() {
        return eurAmount;
    }

    public void setEurAmount(BigDecimal eurAmount) {
        this.eurAmount = eurAmount;
    }
}
