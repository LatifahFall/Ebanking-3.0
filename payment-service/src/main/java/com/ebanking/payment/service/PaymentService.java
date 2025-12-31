package com.ebanking.payment.service;

import com.ebanking.payment.dto.PaymentRequest;
import com.ebanking.payment.dto.PaymentResponse;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.ReversalReason;
import com.ebanking.payment.exception.PaymentNotFoundException;
import com.ebanking.payment.exception.PaymentValidationException;
import com.ebanking.payment.kafka.event.FraudDetectedEvent;
import com.ebanking.payment.kafka.event.PaymentCompletedEvent;
import com.ebanking.payment.kafka.event.PaymentReversedEvent;
import com.ebanking.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentValidationService validationService;
    private final PaymentRuleService ruleService;
    private final FraudDetectionService fraudDetectionService;
    private final PaymentProcessingService processingService;
    private final PaymentEventProducer eventProducer;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request, UUID userId) {
        // Validate payment request
        validationService.validatePaymentRequest(request)
                .block(); // Block for synchronous processing

        // Create payment entity
        Payment payment = Payment.builder()
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentType(request.getPaymentType())
                .status(PaymentStatus.PENDING)
                .beneficiaryName(request.getBeneficiaryName())
                .reference(request.getReference())
                .description(request.getDescription())
                .userId(userId)
                .build();

        payment = paymentRepository.save(payment);

        // Evaluate payment rules
        if (!ruleService.evaluatePayment(payment)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentValidationException("Payment failed rule validation");
        }

        // Fraud detection
        FraudDetectionService.FraudAnalysisResult fraudResult = fraudDetectionService.analyzeTransaction(payment);
        if (fraudResult.isFraud()) {
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
            
            // Publish fraud event
            FraudDetectedEvent fraudEvent = FraudDetectedEvent.builder()
                    .fraudId(UUID.randomUUID())
                    .paymentId(payment.getId())
                    .accountId(payment.getFromAccountId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .fraudType(fraudResult.getFraudType().name())
                    .reason(fraudResult.getReason())
                    .detectedAt(LocalDateTime.now())
                    .action("BLOCKED")
                    .build();
            eventProducer.publishFraudDetected(fraudEvent);
            
            throw new PaymentValidationException("Fraud detected: " + fraudResult.getReason());
        }

        // Process payment based on type
        if (payment.getPaymentType() == com.ebanking.payment.entity.PaymentType.INSTANT) {
            payment = processingService.processInstantPayment(payment.getId());
        } else {
            payment = processingService.processStandardPayment(payment.getId());
        }

        // Publish payment completed event
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .accountId(payment.getFromAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionType(payment.getPaymentType().name())
                .status(payment.getStatus().name())
                .completedAt(payment.getCompletedAt())
                .build();
        eventProducer.publishPaymentCompleted(event);

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse processPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment cannot be processed. Status: " + payment.getStatus());
        }

        payment = processingService.processStandardPayment(paymentId);

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .accountId(payment.getFromAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionType(payment.getPaymentType().name())
                .status(payment.getStatus().name())
                .completedAt(payment.getCompletedAt())
                .build();
        eventProducer.publishPaymentCompleted(event);

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed payment");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment = paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse reversePayment(UUID paymentId, ReversalReason reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Can only reverse completed payments");
        }

        payment.setStatus(PaymentStatus.REVERSED);
        payment.setReversedAt(LocalDateTime.now());
        payment.setReversalReason(reason.name());
        payment = paymentRepository.save(payment);

        PaymentReversedEvent event = PaymentReversedEvent.builder()
                .paymentId(payment.getId())
                .accountId(payment.getFromAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .reversalReason(reason.name())
                .originalPaymentDate(payment.getCompletedAt())
                .reversedAt(payment.getReversedAt())
                .build();
        eventProducer.publishPaymentReversed(event);

        return mapToResponse(payment);
    }

    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByAccount(UUID accountId, PaymentStatus status, Pageable pageable) {
        List<Payment> payments;
        if (status != null) {
            payments = paymentRepository.findByFromAccountIdAndStatus(accountId, status);
        } else {
            payments = paymentRepository.findByFromAccountId(accountId);
        }
        
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .fromAccountId(payment.getFromAccountId())
                .toAccountId(payment.getToAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .beneficiaryName(payment.getBeneficiaryName())
                .reference(payment.getReference())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .completedAt(payment.getCompletedAt())
                .reversedAt(payment.getReversedAt())
                .reversalReason(payment.getReversalReason())
                .userId(payment.getUserId())
                .build();
    }
}

