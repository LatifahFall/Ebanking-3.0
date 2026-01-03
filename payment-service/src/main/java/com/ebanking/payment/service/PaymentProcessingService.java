package com.ebanking.payment.service;

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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment processStandardPayment(UUID paymentId) {
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
    public Payment processInstantPayment(UUID paymentId) {
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
    public Payment processBiometricPayment(UUID paymentId) {
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
            // Traitement du paiement (débit du compte)
            // TODO: Appel au Account Service pour débit

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
    public Payment processQRCodePayment(UUID paymentId) {
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
            // Traitement du paiement (débit du compte)
            // TODO: Appel au Account Service pour débit

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
    public Payment updatePaymentStatus(UUID paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        payment.setStatus(status);
        return paymentRepository.save(payment);
    }
}

