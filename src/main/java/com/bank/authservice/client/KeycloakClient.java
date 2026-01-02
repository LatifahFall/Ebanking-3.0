package com.bank.authservice.client;

import com.bank.authservice.dto.TokenResponse;
import com.bank.authservice.exception.KeycloakServiceException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Keycloak Client - Spring Boot 3 compatible
 * - Stateless
 * - Circuit Breaker (Resilience4j)
 * - Timeout strict
 * - Retry limité
 */
@Service
@Slf4j
public class KeycloakClient {

    /* ================= CONSTANTS ================= */

    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String REFRESH_TOKEN = "refresh_token";

    /* ================= CONFIG ================= */

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    /* ================= INTERNAL ================= */

    private WebClient webClient;
    private String tokenEndpoint;
    private String logoutEndpoint;
    private final CircuitBreaker circuitBreaker;

    public KeycloakClient(CircuitBreakerRegistry registry) {
        this.circuitBreaker = registry.circuitBreaker("keycloak");
    }

    /* ================= INIT ================= */

    @PostConstruct
    public void init() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5))
                .compress(true);

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        this.tokenEndpoint =
                authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        this.logoutEndpoint =
                authServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

        log.info("KeycloakClient initialized");
    }

    /* ================= LOGIN ================= */

    public TokenResponse getToken(String username, String password) {
        return circuitBreaker.executeSupplier(() -> {
            try {
                return webClient.post()
                        .uri(tokenEndpoint)
                        .body(BodyInserters.fromFormData("grant_type", "password")
                                .with(CLIENT_ID, clientId)
                                .with(CLIENT_SECRET, clientSecret)
                                .with("username", username)
                                .with("password", password))
                        .retrieve()
                        .bodyToMono(TokenResponse.class)
                        .retryWhen(Retry.backoff(2, Duration.ofMillis(150))
                                .filter(this::isRetryable))
                        .block();

            } catch (WebClientResponseException e) {
                handleKeycloakError(e);
                return null; // unreachable
            } catch (Exception e) {
                throw new KeycloakServiceException("Keycloak authentication failed", e);
            }
        });
    }

    /* ================= REFRESH ================= */

    public TokenResponse refreshToken(String refreshToken) {
        return circuitBreaker.executeSupplier(() -> {
            try {
                return webClient.post()
                        .uri(tokenEndpoint)
                        .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                                .with(CLIENT_ID, clientId)
                                .with(CLIENT_SECRET, clientSecret)
                                .with(REFRESH_TOKEN, refreshToken))
                        .retrieve()
                        .bodyToMono(TokenResponse.class)
                        .retryWhen(Retry.backoff(2, Duration.ofMillis(150))
                                .filter(this::isRetryable))
                        .block();

            } catch (WebClientResponseException e) {
                handleKeycloakError(e);
                return null;
            } catch (Exception e) {
                throw new KeycloakServiceException("Token refresh failed", e);
            }
        });
    }

    /* ================= LOGOUT ================= */

    public void logout(String refreshToken) {
        circuitBreaker.executeRunnable(() -> {
            try {
                webClient.post()
                        .uri(logoutEndpoint)
                        .body(BodyInserters.fromFormData(CLIENT_ID, clientId)
                                .with(CLIENT_SECRET, clientSecret)
                                .with(REFRESH_TOKEN, refreshToken))
                        .retrieve()
                        .toBodilessEntity()
                        .block();

            } catch (WebClientResponseException e) {
                // 400 = token déjà invalide → pas bloquant
                if (e.getStatusCode().value() != 400) {
                    handleKeycloakError(e);
                }
            } catch (Exception e) {
                throw new KeycloakServiceException("Logout failed", e);
            }
        });
    }

    /* ================= UTIL ================= */

    private boolean isRetryable(Throwable t) {
        if (t instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError();
        }
        return false;
    }

    private void handleKeycloakError(WebClientResponseException e) {
        var status = e.getStatusCode(); // HttpStatusCode (Spring Boot 3)

        if (status.value() == 400 || status.value() == 401) {
            throw new KeycloakServiceException("Invalid credentials");
        }

        if (status.is5xxServerError()) {
            throw new KeycloakServiceException("Keycloak unavailable");
        }

        throw new KeycloakServiceException("Keycloak error: HTTP " + status.value());
    }
}
