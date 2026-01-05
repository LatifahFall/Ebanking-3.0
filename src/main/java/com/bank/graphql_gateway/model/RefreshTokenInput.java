package com.bank.graphql_gateway.model;

public class RefreshTokenInput {
    private String refresh_token;

    public RefreshTokenInput() {
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }
}
