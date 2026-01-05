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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
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
        log.info("Account created with ID: {} for userId: {}", account.getId(), request.getUserId());

        kafkaProducer.publishAccountCreated(AccountCreatedEvent.builder()
                .accountId(account.getId())                    // Long → OK
                .accountNumber(account.getAccountNumber())     // String → OK
                .userId(account.getUserId())                   // Long → OK (si userId est Long dans l'événement)
                .accountType(account.getAccountType().name())  // ← CORRIGÉ : enum → String
                .currency(account.getCurrency())               // String → OK
                .initialBalance(account.getBalance())          // BigDecimal → OK
                .status(account.getStatus().name())            // ← AJOUTÉ et CORRIGÉ : enum → String
                .createdAt(Instant.now())                      // Instant → OK
                .accountName("Compte principal")               // Optionnel, mais utile (ou retire si pas dans l'événement)
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

        // Publication uniquement si le statut a changé
        if (previousStatus != account.getStatus()) {
            kafkaProducer.publishAccountUpdated(AccountUpdatedEvent.builder()
                    .accountId(account.getId())
                    .previousStatus(previousStatus.name())        // ← CORRIGÉ
                    .newStatus(account.getStatus().name())        // ← CORRIGÉ
                    .updateReason("Status changed")
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

        if (account.isSuspended()) {
            log.warn("Account {} is already suspended", id);
            return;
        }
        if (account.isClosed()) {
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
                .build());

        log.warn("Account {} suspended by {}", id, suspendedBy);
    }
    public void suspendAccountForFraud(Long accountId, String fraudReason) {
        String fullReason = "FRAUD_DETECTED: " + fraudReason;
        suspendAccount(accountId, fullReason, "fraud-service");
        log.warn("🔴 AUTOMATIC FRAUD SUSPENSION | accountId: {} | reason: {}", accountId, fraudReason);
    }

    public void closeAccount(Long id, String closureReason, String closedBy) {
        Account account = findAccountById(id);

        if (account.isClosed()) {
            log.warn("Account {} is already closed", id);
            return;
        }

        account.freezeFinalBalance(); // ← vérifie que solde = 0 et fige finalBalance

        account.setStatus(AccountStatus.CLOSED);
        account.setClosureReason(closureReason);
        account.setClosedBy(closedBy);
        account.setClosedAt(LocalDateTime.now());

        accountRepository.save(account);

        kafkaProducer.publishAccountClosed(AccountClosedEvent.builder()
                .accountId(id)
                .closureReason(closureReason)
                .finalBalance(account.getFinalBalance())
                .timestamp(LocalDateTime.now())
                .build());

        log.warn("Account {} successfully closed by {}", id, closedBy);
    }

    // =========================================================================
    // BALANCE & TRANSACTIONS
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
        return transactionRepository
                .findByAccountIdOrderByCreatedAtDesc(id)
                .stream()
                .limit(limit)
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
    // KAFKA EVENT PROCESSING
    // =========================================================================
    public void processPaymentCompleted(PaymentCompletedEvent event) {
        Account account = findAccountById(event.getAccountId());
        account.assertOperational(); // bloque si compte suspendu ou fermé

        BigDecimal previousBalance = account.getBalance();

        // Détermination du sens de la transaction selon le contrat payment-service
        // transactionType = PAYMENT | TRANSFER | WITHDRAWAL
        // Convention réaliste :
        // - TRANSFER → généralement virement entrant = crédit
        // - PAYMENT / WITHDRAWAL → paiement ou retrait sortant = débit
        boolean isCredit = "TRANSFER".equals(event.getTransactionType());

        if (isCredit) {
            account.credit(event.getAmount());
        } else {
            account.debit(event.getAmount());
        }

        TransactionType transactionType = isCredit ? TransactionType.CREDIT : TransactionType.DEBIT;
        ChangeType changeType = isCredit ? ChangeType.CREDIT : ChangeType.DEBIT;

        String description = event.getMetadata() != null
                ? event.getMetadata().getOrDefault("description", "Payment completed")
                : "Payment completed";

        String merchantId = event.getMetadata() != null
                ? event.getMetadata().get("merchantId")
                : null;

        Transaction transaction = Transaction.builder()
                .accountId(account.getId())
                .type(transactionType)
                .amount(event.getAmount())
                .balanceAfter(account.getBalance())
                .reference(event.getPaymentId().toString())
                .description(description)
                .build();

        accountRepository.save(account);
        transactionRepository.save(transaction);

        kafkaProducer.publishBalanceChanged(BalanceChangedEvent.builder()
                .accountId(account.getId())
                .previousBalance(previousBalance)
                .newBalance(account.getBalance())
                .changeAmount(event.getAmount())
                .changeType(changeType)
                .transactionReference(event.getPaymentId().toString())
                .timestamp(event.getCompletedAt() != null
                        ? event.getCompletedAt().atZone(ZoneId.of("UTC")).toLocalDateTime()
                        : LocalDateTime.now())
                .build());

        log.info("[PAYMENT-COMPLETED] {} applied | paymentId: {} | accountId: {} | amount: {} {} | new balance: {}",
                isCredit ? "Credit" : "Debit",
                event.getPaymentId(), account.getId(), event.getAmount(), event.getCurrency(), account.getBalance());
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
                .description("Payment reversal")
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
                .build());

        log.info("Payment {} reversed on account {}", event.getPaymentId(), account.getId());
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + id));
    }

    private String generateUniqueAccountNumber() {
        return "ACC-" + System.nanoTime();
    }

    private BigDecimal calculateOpeningBalance(Long accountId, LocalDateTime startDate) {
        List<Transaction> priorTransactions = transactionRepository
                .findByAccountIdAndCreatedAtBefore(accountId, startDate);

        return priorTransactions.stream()
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
                .finalBalance(account.getFinalBalance()) // si tu ajoutes ce champ dans l'entity Account
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
