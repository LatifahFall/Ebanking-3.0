package com.ebanking.payment.service;

import com.ebanking.payment.entity.FraudType;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final PaymentRepository paymentRepository;
    private final Set<Long> blacklist = new HashSet<>(); // In-memory cache (can be replaced with Redis)

    public FraudAnalysisResult analyzeTransaction(Payment payment) {
        if (checkBlacklist(payment.getFromAccountId())) {
            return FraudAnalysisResult.builder()
                    .isFraud(true)
                    .fraudType(FraudType.BLACKLIST)
                    .reason("Account is blacklisted")
                    .build();
        }

        if (detectAnomaly(payment)) {
            return FraudAnalysisResult.builder()
                    .isFraud(true)
                    .fraudType(FraudType.SUSPICIOUS_AMOUNT)
                    .reason("Suspicious transaction pattern detected")
                    .build();
        }

        return FraudAnalysisResult.builder()
                .isFraud(false)
                .build();
    }

    public boolean checkBlacklist(Long accountId) {
        return blacklist.contains(accountId);
    }

    public void addToBlacklist(Long accountId) {
        blacklist.add(accountId);
        log.warn("Account {} added to blacklist", accountId);
    }

    public void removeFromBlacklist(Long accountId) {
        blacklist.remove(accountId);
        log.info("Account {} removed from blacklist", accountId);
    }

    public boolean detectAnomaly(Payment payment) {
        // Check for suspicious amount
        BigDecimal suspiciousAmountThreshold = new BigDecimal("10000");
        if (payment.getAmount().compareTo(suspiciousAmountThreshold) > 0) {
            log.warn("Suspicious amount detected: {} for account {}", payment.getAmount(), payment.getFromAccountId());
            return true;
        }

        // Check for frequency anomaly (multiple payments in short time)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Payment> recentPayments = paymentRepository.findByFromAccountIdAndStatus(
                payment.getFromAccountId(), PaymentStatus.COMPLETED);
        
        long recentCount = recentPayments.stream()
                .filter(p -> p.getCreatedAt().isAfter(oneHourAgo))
                .count();
        
        if (recentCount > 10) {
            log.warn("Frequency anomaly detected: {} payments in last hour for account {}", 
                    recentCount, payment.getFromAccountId());
            return true;
        }

        return false;
    }

    @lombok.Data
    @lombok.Builder
    public static class FraudAnalysisResult {
        private boolean isFraud;
        private FraudType fraudType;
        private String reason;
    }
}

