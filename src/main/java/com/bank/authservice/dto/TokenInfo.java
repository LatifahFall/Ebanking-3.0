package com.bank.authservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class TokenInfo {
    private boolean valid;
    private String username;
    private String email;
    private String subject;
    private Instant issuedAt;
    private Instant expiresAt;
    private List<String> roles;
}