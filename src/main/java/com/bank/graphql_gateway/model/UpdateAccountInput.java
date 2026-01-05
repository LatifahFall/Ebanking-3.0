package com.bank.graphql_gateway.model;

public class UpdateAccountInput {
    private String accountType;
    private String currency;

    public UpdateAccountInput() {
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
