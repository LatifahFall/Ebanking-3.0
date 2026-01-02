package com.bank.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
