// ============================================================================
// AccountService.java – Version complète avec transaction.completed
// ============================================================================
package com.banking.account.service;

import com.banking.account.dto.*;
import com.banking.account.kafka.KafkaProducer;
import com.banking.account.kafka.event.*;
import com.banking.account.model.Account;
import com.banking.account.model.Account.AccountStatus;
import com.banking.account.model.Transaction;
import com.banking.account.model.Transaction.TransactionType;
import com.banking.account.repository.AccountRepository;
import com.banking.account.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaProducer kafkaProducer;

    // =========================================================================
    // ACCOUNT CRUD
    // =========================================================================
    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = Account.builder()
                .userId(request.getUserId())
                .accountNumber(generateUniqueAccountNumber())
                .accountType(request.getAccountType())
                .currency(request.getCurrency())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        account = accountRepository.save(account);

        log.info("Account created | accountId: {} | userId: {} | type: {} | currency: {}",
                account.getId(), request.getUserId(), account.getAccountType(), account.getCurrency());

        kafkaProducer.publishAccountCreated(AccountCreatedEvent.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .accountType(account.getAccountType().name())
                .currency(account.getCurrency())
                .initialBalance(account.getBalance())
                .status(account.getStatus().name())
                .createdAt(Instant.now())
                .build());

        return mapToAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        return mapToAccountResponse(findAccountById(id));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    public AccountResponse updateAccount(Long id, UpdateAccountRequest request) {
        Account account = findAccountById(id);
        AccountStatus previousStatus = account.getStatus();

        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }
        if (request.getCurrency() != null) {
            account.setCurrency(request.getCurrency());
        }

        accountRepository.save(account);

        if (previousStatus != account.getStatus()) {
            kafkaProducer.publishAccountUpdated(AccountUpdatedEvent.builder()
                    .accountId(account.getId())
                    .previousStatus(previousStatus.name())
                    .newStatus(account.getStatus().name())
                    .updateReason("Manual status update")
                    .updatedAt(Instant.now())
                    .build());
        }

        return mapToAccountResponse(account);
    }

    // =========================================================================
    // ACCOUNT STATUS OPERATIONS
    // =========================================================================
    public void suspendAccount(Long id, String reason, String suspendedBy) {
        Account account = findAccountById(id);
        if (account.getStatus() == AccountStatus.SUSPENDED) {
            log.warn("Account {} is already suspended", id);
            return;
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot suspend a closed account");
        }

        account.setStatus(AccountStatus.SUSPENDED);
        account.setSuspensionReason(reason);
        account.setSuspendedBy(suspendedBy);
        account.setSuspendedAt(LocalDateTime.now());
        accountRepository.save(account);

        kafkaProducer.publishAccountSuspended(AccountSuspendedEvent.builder()
                .accountId(id)
                .suspensionReason(reason)
                .suspendedBy(suspendedBy)
                .suspendedAt(Instant.now())
                .build());

        log.warn("Account {} suspended by {} | reason: {}", id, suspendedBy, reason);
    }

    public void suspendAccountForFraud(Long accountId, String fraudReason) {
        String fullReason = "FRAUD_DETECTED: " + fraudReason;
        suspendAccount(accountId, fullReason, "fraud-service");
        log.warn("AUTOMATIC FRAUD SUSPENSION | accountId: {} | reason: {}", accountId, fraudReason);
    }

    public void closeAccount(Long id, String closureReason, String closedBy) {
        Account account = findAccountById(id);
        if (account.getStatus() == AccountStatus.CLOSED) {
            log.warn("Account {} is already closed", id);
            return;
        }

        account.freezeFinalBalance(); // Vérifie solde = 0
        account.setStatus(AccountStatus.CLOSED);
        account.setClosureReason(closureReason);
        account.setClosedBy(closedBy);
        account.setClosedAt(LocalDateTime.now());
        accountRepository.save(account);

        kafkaProducer.publishAccountClosed(AccountClosedEvent.builder()
                .accountId(id)
                .closureReason(closureReason)
                .finalBalance(account.getFinalBalance())
                .closedAt(Instant.now())
                .build());

        log.warn("Account {} closed by {} | reason: {}", id, closedBy, closureReason);
    }

    // =========================================================================
    // BALANCE & STATEMENTS
    // =========================================================================
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long id) {
        Account account = findAccountById(id);
        return BalanceResponse.builder()
                .accountId(id)
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .availableBalance(account.getBalance())
                .currency(account.getCurrency())
                .lastUpdated(account.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(Long id, int limit) {
        return transactionRepository.findTopByAccountIdOrderByCreatedAtDesc(id, limit).stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountStatementResponse getAccountStatement(Long id, LocalDateTime startDate, LocalDateTime endDate) {
        Account account = findAccountById(id);
        List<Transaction> transactions = transactionRepository
                .findByAccountIdAndCreatedAtBetweenOrderByCreatedAtAsc(id, startDate, endDate);

        BigDecimal openingBalance = calculateOpeningBalance(id, startDate);

        return AccountStatementResponse.builder()
                .accountId(id)
                .accountNumber(account.getAccountNumber())
                .currency(account.getCurrency())
                .periodStart(startDate)
                .periodEnd(endDate)
                .openingBalance(openingBalance)
                .closingBalance(account.getBalance())
                .transactions(transactions.stream()
                        .map(this::mapToTransactionResponse)
                        .collect(Collectors.toList()))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // =========================================================================
    // KAFKA EVENT PROCESSING (payment-service → account-service)
    // =========================================================================
    public void processPaymentCompleted(PaymentCompletedEvent event) {
        Account account = findAccountById(event.getAccountId());
        account.assertOperational();

        BigDecimal previousBalance = account.getBalance();
        boolean isCredit = "TRANSFER".equalsIgnoreCase(event.getTransactionType());
        ChangeType changeType = isCredit ? ChangeType.CREDIT : ChangeType.DEBIT;
        TransactionType txType = isCredit ? TransactionType.CREDIT : TransactionType.DEBIT;

        if (isCredit) {
            account.credit(event.getAmount());
        } else {
            account.debit(event.getAmount());
        }

        String description = event.getDescription() != null ? event.getDescription() : "Payment completed";

        Transaction transaction = Transaction.builder()
                .accountId(account.getId())
                .type(txType)
                .amount(event.getAmount())
                .balanceAfter(account.getBalance())
                .reference(event.getPaymentId().toString())
                .description(description)
                .createdAt(event.getCompletedAt() != null
                        ? event.getCompletedAt().atZone(ZoneId.of("UTC")).toLocalDateTime()
                        : LocalDateTime.now())
                .build();

        accountRepository.save(account);
        transactionRepository.save(transaction);

        // 1. Balance changed (analytics + notification)
        kafkaProducer.publishBalanceChanged(BalanceChangedEvent.builder()
                .accountId(account.getId())
                .previousBalance(previousBalance)
                .newBalance(account.getBalance())
                .changeAmount(event.getAmount())
                .changeType(changeType)
                .transactionReference(event.getPaymentId().toString())
                .timestamp(Instant.now())
                .build());

        // 2. Transaction completed (notification + audit)
        kafkaProducer.publishTransactionCompleted(TransactionCompletedEvent.builder()
                .transactionId(UUID.fromString(event.getPaymentId().toString())) // ou UUID.randomUUID() si pas UUID
                .accountId(account.getId())
                .userId(account.getUserId())
                .accountNumber(account.getAccountNumber())
                .amount(event.getAmount())
                .currency(account.getCurrency())
                .transactionType(event.getTransactionType())
                .description(description)
                .reference(event.getPaymentId().toString())
                .balanceBefore(previousBalance)
                .balanceAfter(account.getBalance())
                .completedAt(Instant.now())
                .status("SUCCESS")
                .sensitive(true)
                .build());

        log.info("[PAYMENT-COMPLETED] Applied | paymentId: {} | accountId: {} | {} {} | new balance: {}",
                event.getPaymentId(), account.getId(),
                isCredit ? "Credit" : "Debit", event.getAmount(), account.getBalance());
    }

    public void processPaymentReversed(PaymentReversedEvent event) {
        Account account = findAccountById(event.getAccountId());
        account.assertOperational();

        if (account.getBalance().compareTo(event.getAmount()) < 0) {
            log.error("Insufficient balance to reverse payment {} on account {}", event.getPaymentId(), account.getId());
            return;
        }

        BigDecimal previousBalance = account.getBalance();
        account.debit(event.getAmount());

        Transaction transaction = Transaction.builder()
                .accountId(account.getId())
                .type(TransactionType.DEBIT)
                .amount(event.getAmount())
                .balanceAfter(account.getBalance())
                .reference(event.getPaymentId().toString() + "-REV")
                .description("Payment reversal - " + event.getReversalReason())
                .createdAt(LocalDateTime.now())
                .build();

        accountRepository.save(account);
        transactionRepository.save(transaction);

        kafkaProducer.publishBalanceChanged(BalanceChangedEvent.builder()
                .accountId(account.getId())
                .previousBalance(previousBalance)
                .newBalance(account.getBalance())
                .changeAmount(event.getAmount())
                .changeType(ChangeType.DEBIT)
                .transactionReference(event.getPaymentId().toString() + "-REV")
                .timestamp(Instant.now())
                .build());

        kafkaProducer.publishTransactionCompleted(TransactionCompletedEvent.builder()
                .transactionId(UUID.randomUUID())
                .accountId(account.getId())
                .userId(account.getUserId())
                .accountNumber(account.getAccountNumber())
                .amount(event.getAmount())
                .currency(account.getCurrency())
                .transactionType("REVERSAL")
                .description("Reversal: " + event.getReversalReason())
                .reference(event.getPaymentId().toString() + "-REV")
                .balanceBefore(previousBalance)
                .balanceAfter(account.getBalance())
                .completedAt(Instant.now())
                .status("REVERSED")
                .sensitive(true)
                .build());

        log.info("Payment {} reversed on account {} | reason: {}", event.getPaymentId(), account.getId(), event.getReversalReason());
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================
    private Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + id));
    }

    private String generateUniqueAccountNumber() {
        return "FR76" + String.format("%020d", System.nanoTime() % 100_000_000_000_000_000L);
    }

    private BigDecimal calculateOpeningBalance(Long accountId, LocalDateTime startDate) {
        return transactionRepository.findByAccountIdAndCreatedAtBefore(accountId, startDate)
                .stream()
                .map(t -> t.getType() == TransactionType.CREDIT ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private AccountResponse mapToAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .accountType(account.getAccountType())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .suspendedAt(account.getSuspendedAt())
                .closedAt(account.getClosedAt())
                .suspensionReason(account.getSuspensionReason())
                .closureReason(account.getClosureReason())
                .finalBalance(account.getFinalBalance())
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .accountId(t.getAccountId())
                .type(t.getType())
                .amount(t.getAmount())
                .balanceAfterTransaction(t.getBalanceAfter())
                .description(t.getDescription())
                .reference(t.getReference())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
