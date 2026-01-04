package com.ebanking.payment.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class AccountServiceClient {

    private final WebClient webClient;

    public AccountServiceClient(@org.springframework.beans.factory.annotation.Qualifier("accountServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Account> getAccount(Long accountId) {
        return webClient.get()
                .uri("/api/accounts/{accountId}", accountId)
                .retrieve()
                .bodyToMono(Account.class)
                .onErrorMap(WebClientResponseException.class, this::mapException)
                .timeout(java.time.Duration.ofSeconds(10));
    }

    public Mono<BigDecimal> checkBalance(Long accountId) {
        return webClient.get()
                .uri("/api/accounts/{accountId}/balance", accountId)
                .retrieve()
                .bodyToMono(BigDecimal.class)
                .onErrorMap(WebClientResponseException.class, this::mapException)
                .timeout(java.time.Duration.ofSeconds(10));
    }

    public Mono<Boolean> validateAccountStatus(Long accountId) {
        return webClient.get()
                .uri("/api/accounts/{accountId}/status", accountId)
                .retrieve()
                .bodyToMono(AccountStatus.class)
                .map(status -> status == AccountStatus.ACTIVE)
                .onErrorMap(WebClientResponseException.class, this::mapException)
                .timeout(java.time.Duration.ofSeconds(10));
    }

    /**
     * Débite un compte avec le montant spécifié
     * POST /api/accounts/{accountId}/debit
     */
    public Mono<Account> debitAccount(Long accountId, BigDecimal amount, String reference) {
        DebitRequest request = new DebitRequest(amount, reference);
        
        return webClient.post()
                .uri("/api/accounts/{accountId}/debit", accountId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Account.class)
                .onErrorMap(WebClientResponseException.class, this::mapException)
                .timeout(java.time.Duration.ofSeconds(10));
    }

    /**
     * Crédite un compte avec le montant spécifié
     * POST /api/accounts/{accountId}/credit
     */
    public Mono<Account> creditAccount(Long accountId, BigDecimal amount, String reference) {
        CreditRequest request = new CreditRequest(amount, reference);
        
        return webClient.post()
                .uri("/api/accounts/{accountId}/credit", accountId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Account.class)
                .onErrorMap(WebClientResponseException.class, this::mapException)
                .timeout(java.time.Duration.ofSeconds(10));
    }

    // DTOs pour les opérations de débit/crédit
    private record DebitRequest(BigDecimal amount, String reference) {}
    private record CreditRequest(BigDecimal amount, String reference) {}

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

