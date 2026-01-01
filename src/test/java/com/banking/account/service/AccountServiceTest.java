package com.banking.account.service;

import com.banking.account.config.TestContainersConfig;
import com.banking.account.dto.*;
import com.banking.account.kafka.KafkaProducer;
import com.banking.account.kafka.event.*;
import com.banking.account.model.Account.AccountStatus;
import com.banking.account.model.Account.AccountType;
import com.banking.account.model.Transaction.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @MockBean
    private KafkaProducer kafkaProducer;

    @Test
    void shouldCreateAccountAndPublishAccountCreatedEvent() {
        // Given
        CreateAccountRequest request = CreateAccountRequest.builder()
                .userId(1001L)
                .accountType(AccountType.CHECKING)
                .currency("EUR")
                .initialBalance(BigDecimal.valueOf(1000.00))
                .build();

        // When
        AccountResponse response = accountService.createAccount(request);

        // Then
        assertThat(response)
                .isNotNull()
                .extracting(
                        AccountResponse::getUserId,
                        AccountResponse::getAccountType,
                        AccountResponse::getCurrency,
                        AccountResponse::getBalance,
                        AccountResponse::getStatus
                )
                .containsExactly(
                        1001L,
                        AccountType.CHECKING,
                        "EUR",
                        BigDecimal.valueOf(1000.00),
                        AccountStatus.ACTIVE
                );

        assertThat(response.getAccountNumber()).startsWith("ACC-");
        assertThat(response.getCreatedAt()).isNotNull();

        verify(kafkaProducer, times(1)).publishAccountCreated(any(AccountCreatedEvent.class));
    }

    @Test
    void shouldSuspendAccountAndPublishSuspendedEvent() {
        // Given
        AccountResponse created = accountService.createAccount(CreateAccountRequest.builder()
                .userId(2001L)
                .accountType(AccountType.SAVINGS)
                .currency("EUR")
                .build());

        // When
        accountService.suspendAccount(created.getId(), "FRAUD_SUSPICION", "admin-007");

        // Then
        AccountResponse suspended = accountService.getAccountById(created.getId());
        assertThat(suspended.getStatus()).isEqualTo(AccountStatus.SUSPENDED);
        assertThat(suspended.getSuspendedAt()).isNotNull();

        verify(kafkaProducer, times(1)).publishAccountSuspended(any(AccountSuspendedEvent.class));
    }

    @Test
    void shouldNotAllowClosingAccountWithNonZeroBalance() {
        // Given
        AccountResponse account = accountService.createAccount(CreateAccountRequest.builder()
                .userId(3001L)
                .accountType(AccountType.CHECKING)
                .currency("EUR")
                .initialBalance(BigDecimal.valueOf(500))
                .build());

        // When / Then
        assertThatThrownBy(() -> accountService.closeAccount(account.getId(), "CLIENT_REQUEST", "client-3001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-zero balance");

        verify(kafkaProducer, never()).publishAccountClosed(any(AccountClosedEvent.class));
    }

    @Test
    void shouldCreditAccountOnIncomingTransfer() {
        // Given
        AccountResponse account = accountService.createAccount(CreateAccountRequest.builder()
                .userId(4001L)
                .accountType(AccountType.CHECKING)
                .currency("EUR")
                .build());

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .accountId(account.getId())
                .amount(BigDecimal.valueOf(750.50))
                .currency("EUR")
                .transactionType("TRANSFER")
                .completedAt(Instant.now())
                .metadata(Map.of("description", "Virement reçu de Jean"))
                .build();

        // When
        accountService.processPaymentCompleted(event);

        // Then
        BalanceResponse balance = accountService.getBalance(account.getId());
        assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(750.50));

        verify(kafkaProducer, times(1)).publishBalanceChanged(argThat(e ->
                e.getChangeType() == ChangeType.CREDIT &&
                        e.getChangeAmount().compareTo(BigDecimal.valueOf(750.50)) == 0
        ));
    }

    @Test
    void shouldDebitAccountOnOutgoingPayment() {
        // Given
        AccountResponse account = accountService.createAccount(CreateAccountRequest.builder()
                .userId(4101L)
                .accountType(AccountType.CHECKING)
                .currency("EUR")
                .initialBalance(BigDecimal.valueOf(1000.00))
                .build());

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .accountId(account.getId())
                .amount(BigDecimal.valueOf(300.00))
                .currency("EUR")
                .transactionType("PAYMENT")
                .completedAt(Instant.now())
                .metadata(Map.of("description", "Achat chez Amazon"))
                .build();

        // When
        accountService.processPaymentCompleted(event);

        // Then
        BalanceResponse balance = accountService.getBalance(account.getId());
        assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700.00));

        verify(kafkaProducer, times(1)).publishBalanceChanged(argThat(e ->
                e.getChangeType() == ChangeType.DEBIT &&
                        e.getChangeAmount().compareTo(BigDecimal.valueOf(300.00)) == 0
        ));
    }

    @Test
    void shouldGenerateCorrectAccountStatementWithMultipleTransactions() {
        // Given
        AccountResponse account = accountService.createAccount(CreateAccountRequest.builder()
                .userId(5001L)
                .accountType(AccountType.CHECKING)
                .currency("EUR")
                .build());

        PaymentCompletedEvent credit = PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .accountId(account.getId())
                .amount(BigDecimal.valueOf(1234.56))
                .currency("EUR")
                .transactionType("TRANSFER")
                .build();
        accountService.processPaymentCompleted(credit);

        PaymentCompletedEvent debit = PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .accountId(account.getId())
                .amount(BigDecimal.valueOf(200.00))
                .currency("EUR")
                .transactionType("PAYMENT")
                .build();
        accountService.processPaymentCompleted(debit);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // When
        AccountStatementResponse statement = accountService.getAccountStatement(account.getId(), start, end);

        // Then
        assertThat(statement.getOpeningBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(statement.getClosingBalance()).isEqualByComparingTo(BigDecimal.valueOf(1034.56));
        assertThat(statement.getTransactions())
                .hasSize(2)
                .extracting(TransactionResponse::getType)
                .containsExactly(TransactionType.CREDIT, TransactionType.DEBIT);
    }

    @Test
    void shouldPreventOperationsOnSuspendedAccount() {
        // Given
        AccountResponse account = accountService.createAccount(CreateAccountRequest.builder()
                .userId(6001L)
                .accountType(AccountType.CHECKING)
                .currency("EUR")
                .build());

        accountService.suspendAccount(account.getId(), "MANUAL_REVIEW", "support");

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .accountId(account.getId())
                .amount(BigDecimal.TEN)
                .currency("EUR")
                .transactionType("TRANSFER")
                .build();

        // When / Then
        assertThatThrownBy(() -> accountService.processPaymentCompleted(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("suspended");

        BalanceResponse balance = accountService.getBalance(account.getId());
        assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(kafkaProducer, never()).publishBalanceChanged(any());
    }

    @Test
    void shouldSuspendAccountAutomaticallyOnFraudDetection() {
        // Given
        AccountResponse account = accountService.createAccount(CreateAccountRequest.builder()
                .userId(7001L)
                .accountType(AccountType.CHECKING)
                .currency("EUR")
                .build());

        FraudDetectedEvent fraudEvent = FraudDetectedEvent.builder()
                .fraudId(UUID.randomUUID())
                .paymentId(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(5000))
                .fraudType("SUSPICIOUS_AMOUNT")
                .reason("Transaction inhabituelle de 5000€ détectée")
                .detectedAt(Instant.now())
                .action("BLOCKED")
                .build();

        // When (simulé via méthode publique exposée pour test)
        accountService.suspendAccountForFraud(account.getId(), fraudEvent.getReason());

        // Then
        AccountResponse updated = accountService.getAccountById(account.getId());
        assertThat(updated.getStatus()).isEqualTo(AccountStatus.SUSPENDED);
        assertThat(updated.getSuspensionReason()).contains("FRAUD_DETECTED");

        verify(kafkaProducer, times(1)).publishAccountSuspended(any(AccountSuspendedEvent.class));
    }
}