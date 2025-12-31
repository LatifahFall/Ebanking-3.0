package com.ebanking.payment.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class AccountServiceClient {

    private final WebClient webClient;

    public AccountServiceClient(@org.springframework.beans.factory.annotation.Qualifier("accountServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Account> getAccount(UUID accountId) {
        return webClient.get()
                .uri("/api/accounts/{accountId}", accountId)
                .retrieve()
                .bodyToMono(Account.class)
                .onErrorMap(WebClientResponseException.class, this::mapException)
                .timeout(java.time.Duration.ofSeconds(10));
    }

    public Mono<BigDecimal> checkBalance(UUID accountId) {
        return webClient.get()
                .uri("/api/accounts/{accountId}/balance", accountId)
                .retrieve()
                .bodyToMono(BigDecimal.class)
                .onErrorMap(WebClientResponseException.class, this::mapException)
                .timeout(java.time.Duration.ofSeconds(10));
    }

    public Mono<Boolean> validateAccountStatus(UUID accountId) {
        return webClient.get()
                .uri("/api/accounts/{accountId}/status", accountId)
                .retrieve()
                .bodyToMono(AccountStatus.class)
                .map(status -> status == AccountStatus.ACTIVE)
                .onErrorMap(WebClientResponseException.class, this::mapException)
                .timeout(java.time.Duration.ofSeconds(10));
    }

    private RuntimeException mapException(WebClientResponseException ex) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new AccountNotFoundException("Account not found: " + ex.getMessage());
        } else if (ex.getStatusCode().is5xxServerError()) {
            return new AccountServiceException("Account service error: " + ex.getMessage());
        } else {
            return new AccountServiceException("Error calling account service: " + ex.getMessage());
        }
    }

    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(String message) {
            super(message);
        }
    }

    public static class AccountServiceException extends RuntimeException {
        public AccountServiceException(String message) {
            super(message);
        }
    }
}

