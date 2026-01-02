package com.bank.authservice.service;

import com.bank.authservice.client.KeycloakClient;
import com.bank.authservice.dto.LoginRequest;
import com.bank.authservice.dto.TokenInfo;
import com.bank.authservice.dto.TokenResponse;
import com.bank.authservice.exception.AuthenticationException;
import com.bank.authservice.messaging.AuthEventProducer;
import com.bank.authservice.messaging.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KeycloakClient keycloakClient;
    private final JwtDecoder jwtDecoder;
    private final AuthEventProducer eventProducer;

    /* ================= LOGIN ================= */

    public TokenResponse login(LoginRequest request) {
        try {
            TokenResponse token = keycloakClient.getToken(
                    request.getUsername(),
                    request.getPassword()
            );

            eventProducer.publishLoginSuccess(
                    new UserLoginSuccessEvent(request.getUsername(), Instant.now())
            );

            return token;

        } catch (Exception e) {
            eventProducer.publishLoginFailure(
                    new UserLoginFailureEvent(
                            request.getUsername(),
                            "INVALID_CREDENTIALS",
                            Instant.now()
                    )
            );
            throw new AuthenticationException("Invalid username or password");
        }
    }

    /* ================= REFRESH ================= */

    public TokenResponse refresh(String refreshToken) {
        try {
            TokenResponse token = keycloakClient.refreshToken(refreshToken);

            eventProducer.publishTokenRefreshed(
                    new TokenRefreshedEvent(Instant.now())
            );

            return token;

        } catch (Exception e) {
            throw new AuthenticationException("Token refresh failed");
        }
    }

    /* ================= LOGOUT ================= */

    public void logout(String refreshToken) {
        keycloakClient.logout(refreshToken);

        eventProducer.publishLogout(
                new UserLogoutEvent(Instant.now())
        );
    }

    /* ================= VERIFY ================= */

    public boolean verify(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* ================= TOKEN INFO ================= */

    public TokenInfo getTokenInfo(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);

            List<String> roles = null;
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                roles = (List<String>) realmAccess.get("roles");
            }

            return TokenInfo.builder()
                    .valid(true)
                    .username(jwt.getClaim("preferred_username"))
                    .email(jwt.getClaim("email"))
                    .subject(jwt.getSubject())
                    .issuedAt(jwt.getIssuedAt())
                    .expiresAt(jwt.getExpiresAt())
                    .roles(roles)
                    .build();

        } catch (Exception e) {
            throw new AuthenticationException("Invalid token");
        }
    }
}
