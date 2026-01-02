package com.bank.authservice.controller;

import com.bank.authservice.dto.*;
import com.bank.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for user={}", request.getUsername());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest body) {
        log.info("Refresh request received");
        return ResponseEntity.ok(authService.refresh(body.getRefresh_token()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshRequest body) {
        log.info("Logout request received");
        authService.logout(body.getRefresh_token());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Boolean>> verify(@Valid @RequestBody TokenRequest body) {
        return ResponseEntity.ok(Map.of("valid", authService.verify(body.getToken())));
    }

    @PostMapping("/token-info")
    public ResponseEntity<TokenInfo> tokenInfo(@Valid @RequestBody TokenRequest body) {
        return ResponseEntity.ok(authService.getTokenInfo(body.getToken()));
    }
}
