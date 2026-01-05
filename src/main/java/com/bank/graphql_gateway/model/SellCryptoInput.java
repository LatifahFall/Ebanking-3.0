package com.bank.graphql_gateway.model;

import java.math.BigDecimal;

public class SellCryptoInput {
    private String symbol;
    private BigDecimal cryptoAmount;

    public SellCryptoInput() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getCryptoAmount() {
        return cryptoAmount;
    }

    public void setCryptoAmount(BigDecimal cryptoAmount) {
        this.cryptoAmount = cryptoAmount;
    }
}
