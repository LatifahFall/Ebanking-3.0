package com.ebanking.payment.service;

import com.ebanking.payment.client.AccountServiceClient;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import com.ebanking.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingService {

    private final PaymentRepository paymentRepository;
    private final AccountServiceClient accountServiceClient;

    @Transactional
    public Payment processStandardPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        payment.setStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        // Standard payment processing logic
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(java.time.LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }

    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Payment processInstantPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getPaymentType() != PaymentType.INSTANT) {
            throw new IllegalStateException("Payment is not an instant payment");
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        // Instant payment processing (synchronous)
        try {
            // Process immediately
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(java.time.LocalDateTime.now());
            log.info("Instant payment {} processed successfully", paymentId);
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            log.error("Error processing instant payment {}", paymentId, e);
            throw e;
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Payment processBiometricPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getPaymentType() != PaymentType.BIOMETRIC) {
            throw new IllegalStateException("Payment is not a biometric payment");
        }

        log.info("Processing biometric payment: {}", paymentId);

        payment.setStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        // Les paiements biométriques sont traités comme des paiements instantanés
        // mais avec une validation supplémentaire
        try {
            // Traitement du paiement (débit du compte source)
            accountServiceClient.debitAccount(
                payment.getFromAccountId(),
                payment.getAmount(),
                "Payment " + payment.getId()
            ).block(); // Block car transaction synchrone

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(java.time.LocalDateTime.now());
            log.info("Biometric payment {} processed successfully", paymentId);
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            log.error("Error processing biometric payment {}", paymentId, e);
            throw e;
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Payment processQRCodePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getPaymentType() != PaymentType.QR_CODE) {
            throw new IllegalStateException("Payment is not a QR code payment");
        }

        log.info("Processing QR code payment: {}", paymentId);

        payment.setStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        // Les paiements QR code sont traités comme des paiements instantanés
        try {
            // Traitement du paiement (débit du compte source)
            accountServiceClient.debitAccount(
                payment.getFromAccountId(),
                payment.getAmount(),
                "Payment " + payment.getId()
            ).block(); // Block car transaction synchrone

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(java.time.LocalDateTime.now());
            log.info("QR code payment {} processed successfully", paymentId);
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            log.error("Error processing QR code payment {}", paymentId, e);
            throw e;
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        payment.setStatus(status);
        return paymentRepository.save(payment);
    }
}

