package com.ebanking.payment.repository;

import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment;
    private UUID fromAccountId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        fromAccountId = UUID.randomUUID();
        userId = UUID.randomUUID();

        payment = Payment.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .paymentType(PaymentType.STANDARD)
                .status(PaymentStatus.PENDING)
                .userId(userId)
                .description("Test payment")
                .build();
    }

    @Test
    void shouldSavePayment() {
        Payment saved = paymentRepository.save(payment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFromAccountId()).isEqualTo(fromAccountId);
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldFindByFromAccountId() {
        paymentRepository.save(payment);

        List<Payment> payments = paymentRepository.findByFromAccountId(fromAccountId);

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getFromAccountId()).isEqualTo(fromAccountId);
    }

    @Test
    void shouldFindByStatus() {
        paymentRepository.save(payment);

        List<Payment> payments = paymentRepository.findByStatus(PaymentStatus.PENDING);

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void shouldFindByCreatedAtBetween() {
        LocalDateTime now = LocalDateTime.now();
        payment.setCreatedAt(now);
        paymentRepository.save(payment);

        List<Payment> payments = paymentRepository.findByCreatedAtBetween(
                now.minusDays(1),
                now.plusDays(1)
        );

        assertThat(payments).hasSize(1);
    }

    @Test
    void shouldFindByFromAccountIdAndStatus() {
        paymentRepository.save(payment);

        List<Payment> payments = paymentRepository.findByFromAccountIdAndStatus(
                fromAccountId,
                PaymentStatus.PENDING
        );

        assertThat(payments).hasSize(1);
    }
}

