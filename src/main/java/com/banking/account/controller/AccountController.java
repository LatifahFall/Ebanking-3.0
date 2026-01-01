package com.banking.account.controller;

import com.banking.account.dto.*;
import com.banking.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    // =========================================================================
    // ACCOUNT CRUD OPERATIONS
    // =========================================================================

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        log.info("POST /api/accounts - Creating account for userId: {}", request.getUserId());
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        log.info("GET /api/accounts/{} - Fetching account", id);
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAccountsByUserId(
            @RequestParam Long userId) {
        log.info("GET /api/accounts?userId={} - Fetching accounts", userId);
        List<AccountResponse> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountRequest request) {
        log.info("PUT /api/accounts/{} - Updating account", id);
        AccountResponse response = accountService.updateAccount(id, request);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // ACCOUNT STATUS OPERATIONS
    // =========================================================================

    @PostMapping("/{id}/suspend")
    public ResponseEntity<MessageResponse> suspendAccount(
            @PathVariable Long id,
            @Valid @RequestBody SuspendAccountRequest request) {
        log.warn("POST /api/accounts/{}/suspend - Suspending account", id);
        accountService.suspendAccount(id, request.getReason(), request.getSuspendedBy());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Account suspended successfully")
                .accountId(id)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<MessageResponse> closeAccount(
            @PathVariable Long id,
            @Valid @RequestBody CloseAccountRequest request) {
        log.warn("POST /api/accounts/{}/close - Closing account", id);
        accountService.closeAccount(id, request.getClosureReason(), request.getClosedBy());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Account closed successfully")
                .accountId(id)
                .timestamp(LocalDateTime.now())
                .build());
    }

    // =========================================================================
    // BALANCE & TRANSACTION OPERATIONS
    // =========================================================================

    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long id) {
        log.info("GET /api/accounts/{}/balance - Fetching balance", id);
        BalanceResponse response = accountService.getBalance(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") int limit) {
        log.info("GET /api/accounts/{}/transactions?limit={}", id, limit);
        List<TransactionResponse> transactions =
                accountService.getTransactionHistory(id, limit);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}/statement")
    public ResponseEntity<AccountStatementResponse> getAccountStatement(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate) {
        log.info("GET /api/accounts/{}/statement - Generating statement from {} to {}",
                id, startDate, endDate);
        AccountStatementResponse response =
                accountService.getAccountStatement(id, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}