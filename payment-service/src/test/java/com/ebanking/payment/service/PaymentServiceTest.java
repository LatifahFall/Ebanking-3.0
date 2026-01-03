package com.ebanking.payment.service;

import com.ebanking.payment.dto.PaymentRequest;
import com.ebanking.payment.dto.PaymentResponse;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import com.ebanking.payment.entity.ReversalReason;
import com.ebanking.payment.exception.PaymentNotFoundException;
import com.ebanking.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentValidationService validationService;

    @Mock
    private PaymentRuleService ruleService;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private PaymentProcessingService processingService;

    @Mock
    private PaymentEventProducer eventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private Payment payment;
    private UUID paymentId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        userId = UUID.randomUUID();

        paymentRequest = new PaymentRequest();
        paymentRequest.setFromAccountId(UUID.randomUUID());
        paymentRequest.setToAccountId(UUID.randomUUID());
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrency("EUR");
        paymentRequest.setPaymentType(PaymentType.STANDARD);

        payment = Payment.builder()
                .id(paymentId)
                .fromAccountId(paymentRequest.getFromAccountId())
                .toAccountId(paymentRequest.getToAccountId())
                .amount(paymentRequest.getAmount())
                .currency(paymentRequest.getCurrency())
                .paymentType(paymentRequest.getPaymentType())
                .status(PaymentStatus.COMPLETED)
                .userId(userId)
                .build();
    }

    @Test
    void shouldGetPayment() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPayment(paymentId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(paymentId);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldThrowExceptionWhenPaymentNotFound() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(paymentId))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void shouldCancelPayment() {
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.cancelPayment(paymentId);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldReversePayment() {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(java.time.LocalDateTime.now());
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.reversePayment(paymentId, ReversalReason.CUSTOMER_REQUEST);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REVERSED);
        verify(eventProducer).publishPaymentReversed(any());
    }
}

