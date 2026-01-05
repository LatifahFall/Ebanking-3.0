package com.bank.graphql_gateway.model;

public class TokenValidationInput {
    private String token;

    public TokenValidationInput() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
