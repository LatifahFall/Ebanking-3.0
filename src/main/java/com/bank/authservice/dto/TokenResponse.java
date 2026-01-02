package com.bank.authservice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {
    private String access_token;
    private String refresh_token;
    private long expires_in;
    private long refresh_expires_in;
    private String token_type;
    private String scope;
}