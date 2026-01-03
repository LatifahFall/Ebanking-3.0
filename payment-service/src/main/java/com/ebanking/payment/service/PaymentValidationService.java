package com.ebanking.payment.service;

import com.ebanking.payment.client.Account;
import com.ebanking.payment.client.AccountServiceClient;
import com.ebanking.payment.client.AccountStatus;
import com.ebanking.payment.dto.PaymentRequest;
import com.ebanking.payment.exception.AccountNotFoundException;
import com.ebanking.payment.exception.InsufficientBalanceException;
import com.ebanking.payment.exception.PaymentValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentValidationService {

    private final AccountServiceClient accountServiceClient;

    public Mono<Void> validatePaymentRequest(PaymentRequest request) {
        return validateAccount(request.getFromAccountId())
                .then(validateBalance(request.getFromAccountId(), request.getAmount()))
                .then(Mono.fromRunnable(() -> log.debug("Payment request validated: {}", request.getFromAccountId())));
    }

    public Mono<Account> validateAccount(UUID accountId) {
        return accountServiceClient.getAccount(accountId)
                .doOnNext(account -> {
                    if (account.getStatus() != AccountStatus.ACTIVE) {
                        throw new PaymentValidationException("Account is not active: " + accountId);
                    }
                })
                .onErrorMap(AccountServiceClient.AccountNotFoundException.class, 
                        e -> new AccountNotFoundException("Account not found: " + accountId))
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found: " + accountId)));
    }

    public Mono<Void> validateBalance(UUID accountId, BigDecimal amount) {
        return accountServiceClient.checkBalance(accountId)
                .flatMap(balance -> {
                    if (balance.compareTo(amount) < 0) {
                        return Mono.error(new InsufficientBalanceException(
                                "Insufficient balance. Available: " + balance + ", Required: " + amount));
                    }
                    return Mono.empty();
                });
    }
}

