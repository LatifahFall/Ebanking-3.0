package com.ebanking.payment.service;

import com.ebanking.payment.entity.FraudType;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import com.ebanking.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FraudDetectionService
 * 
 * Security Test Coverage:
 * - Blacklist detection
 * - Suspicious amount detection (>10,000)
 * - Frequency anomaly detection (>10 txn/hour)
 * - Fraud score calculation
 * - Multiple fraud indicators
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FraudDetectionService Security Tests")
class FraudDetectionServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private Payment validPayment;
    private Long testAccountId;

    @BeforeEach
    void setUp() {
        testAccountId = 100L;
        
        validPayment = Payment.builder()
                .id(1L)
                .fromAccountId(testAccountId)
                .toAccountId(200L)
                .amount(BigDecimal.valueOf(500.00))
                .currency("EUR")
                .paymentType(PaymentType.STANDARD)
                .status(PaymentStatus.PENDING)
                .userId(10L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ========================================
    // POSITIVE TESTS - No Fraud Detected
    // ========================================

    @Test
    @DisplayName("Should pass valid low-amount transaction")
    void testAnalyzeTransaction_ValidLowAmount_NoFraud() {
        // Given
        validPayment.setAmount(BigDecimal.valueOf(500.00));
        when(paymentRepository.findByFromAccountIdAndStatus(testAccountId, PaymentStatus.COMPLETED))
                .thenReturn(Collections.emptyList());

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isFalse();
        assertThat(result.getFraudType()).isNull();
        assertThat(result.getReason()).isNull();
    }

    @Test
    @DisplayName("Should pass transaction just below suspicious amount threshold")
    void testAnalyzeTransaction_JustBelowThreshold_NoFraud() {
        // Given
        validPayment.setAmount(BigDecimal.valueOf(9999.99));
        when(paymentRepository.findByFromAccountIdAndStatus(testAccountId, PaymentStatus.COMPLETED))
                .thenReturn(Collections.emptyList());

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isFalse();
    }

    @Test
    @DisplayName("Should pass transaction with normal frequency (<=10 per hour)")
    void testAnalyzeTransaction_NormalFrequency_NoFraud() {
        // Given
        List<Payment> recentPayments = createRecentPayments(5); // Only 5 in last hour
        when(paymentRepository.findByFromAccountIdAndStatus(testAccountId, PaymentStatus.COMPLETED))
                .thenReturn(recentPayments);

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isFalse();
    }

    // ========================================
    // BLACKLIST TESTS
    // ========================================

    @Test
    @DisplayName("FRAUD: Should detect blacklisted account")
    void testAnalyzeTransaction_BlacklistedAccount_DetectsFraud() {
        // Given
        fraudDetectionService.addToBlacklist(testAccountId);

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getFraudType()).isEqualTo(FraudType.BLACKLIST);
        assertThat(result.getReason()).contains("blacklisted");
    }

    @Test
    @DisplayName("Should add account to blacklist")
    void testAddToBlacklist_AddsAccountSuccessfully() {
        // When
        fraudDetectionService.addToBlacklist(testAccountId);

        // Then
        assertThat(fraudDetectionService.checkBlacklist(testAccountId)).isTrue();
    }

    @Test
    @DisplayName("Should remove account from blacklist")
    void testRemoveFromBlacklist_RemovesAccountSuccessfully() {
        // Given
        fraudDetectionService.addToBlacklist(testAccountId);
        assertThat(fraudDetectionService.checkBlacklist(testAccountId)).isTrue();

        // When
        fraudDetectionService.removeFromBlacklist(testAccountId);

        // Then
        assertThat(fraudDetectionService.checkBlacklist(testAccountId)).isFalse();
    }

    @Test
    @DisplayName("Should check blacklist returns false for non-blacklisted account")
    void testCheckBlacklist_NonBlacklistedAccount_ReturnsFalse() {
        // When
        boolean isBlacklisted = fraudDetectionService.checkBlacklist(testAccountId);

        // Then
        assertThat(isBlacklisted).isFalse();
    }

    // ========================================
    // SUSPICIOUS AMOUNT TESTS
    // ========================================

    @Test
    @DisplayName("FRAUD: Should detect suspicious amount (>10,000)")
    void testAnalyzeTransaction_SuspiciousAmount_DetectsFraud() {
        // Given
        validPayment.setAmount(BigDecimal.valueOf(50000.00));

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getFraudType()).isEqualTo(FraudType.SUSPICIOUS_AMOUNT);
        assertThat(result.getReason()).contains("Suspicious transaction pattern");
    }

    @Test
    @DisplayName("FRAUD: Should detect amount exactly at threshold (10,000.01)")
    void testAnalyzeTransaction_AmountAtThreshold_DetectsFraud() {
        // Given
        validPayment.setAmount(BigDecimal.valueOf(10000.01));

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getFraudType()).isEqualTo(FraudType.SUSPICIOUS_AMOUNT);
    }

    @Test
    @DisplayName("FRAUD: Should detect very high amount (>100,000)")
    void testAnalyzeTransaction_VeryHighAmount_DetectsFraud() {
        // Given
        validPayment.setAmount(BigDecimal.valueOf(500000.00));

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getFraudType()).isEqualTo(FraudType.SUSPICIOUS_AMOUNT);
    }

    // ========================================
    // FREQUENCY ANOMALY TESTS
    // ========================================

    @Test
    @DisplayName("FRAUD: Should detect frequency anomaly (>10 transactions per hour)")
    void testAnalyzeTransaction_FrequencyAnomaly_DetectsFraud() {
        // Given
        List<Payment> recentPayments = createRecentPayments(15); // 15 payments in last hour
        when(paymentRepository.findByFromAccountIdAndStatus(testAccountId, PaymentStatus.COMPLETED))
                .thenReturn(recentPayments);
        validPayment.setAmount(BigDecimal.valueOf(100.00)); // Low amount

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getFraudType()).isEqualTo(FraudType.SUSPICIOUS_AMOUNT);
        assertThat(result.getReason()).contains("Suspicious transaction pattern");
    }

    @Test
    @DisplayName("FRAUD: Should detect frequency anomaly at boundary (11 transactions)")
    void testAnalyzeTransaction_FrequencyAtBoundary_DetectsFraud() {
        // Given
        List<Payment> recentPayments = createRecentPayments(11); // Exactly 11 payments
        when(paymentRepository.findByFromAccountIdAndStatus(testAccountId, PaymentStatus.COMPLETED))
                .thenReturn(recentPayments);
        validPayment.setAmount(BigDecimal.valueOf(100.00));

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isTrue();
    }

    @Test
    @DisplayName("Should pass with exactly 10 transactions per hour (boundary)")
    void testAnalyzeTransaction_ExactlyTenTransactions_NoFraud() {
        // Given
        List<Payment> recentPayments = createRecentPayments(10); // Exactly 10 payments
        when(paymentRepository.findByFromAccountIdAndStatus(testAccountId, PaymentStatus.COMPLETED))
                .thenReturn(recentPayments);
        validPayment.setAmount(BigDecimal.valueOf(100.00));

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isFalse();
    }

    // ========================================
    // ANOMALY DETECTION LOGIC TESTS
    // ========================================

    @Test
    @DisplayName("Should detect anomaly for suspicious amount")
    void testDetectAnomaly_SuspiciousAmount_ReturnsTrue() {
        // Given
        validPayment.setAmount(BigDecimal.valueOf(20000.00));

        // When
        boolean hasAnomaly = fraudDetectionService.detectAnomaly(validPayment);

        // Then
        assertThat(hasAnomaly).isTrue();
    }

    @Test
    @DisplayName("Should not detect anomaly for normal amount and frequency")
    void testDetectAnomaly_NormalTransaction_ReturnsFalse() {
        // Given
        validPayment.setAmount(BigDecimal.valueOf(500.00));
        when(paymentRepository.findByFromAccountIdAndStatus(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        // When
        boolean hasAnomaly = fraudDetectionService.detectAnomaly(validPayment);

        // Then
        assertThat(hasAnomaly).isFalse();
    }

    // ========================================
    // EDGE CASES & BOUNDARY TESTS
    // ========================================

    @Test
    @DisplayName("Should handle zero amount gracefully")
    void testAnalyzeTransaction_ZeroAmount_NoFraud() {
        // Given
        validPayment.setAmount(BigDecimal.ZERO);
        when(paymentRepository.findByFromAccountIdAndStatus(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isFalse();
    }

    @Test
    @DisplayName("Should handle negative amount (edge case)")
    void testAnalyzeTransaction_NegativeAmount_NoFraud() {
        // Given
        validPayment.setAmount(BigDecimal.valueOf(-100.00));
        when(paymentRepository.findByFromAccountIdAndStatus(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isFalse();
    }

    @Test
    @DisplayName("Should only count recent payments (within 1 hour)")
    void testAnalyzeTransaction_OldPaymentsIgnored_NoFraud() {
        // Given
        List<Payment> oldPayments = createOldPayments(15); // 15 old payments (>1 hour ago)
        when(paymentRepository.findByFromAccountIdAndStatus(testAccountId, PaymentStatus.COMPLETED))
                .thenReturn(oldPayments);
        validPayment.setAmount(BigDecimal.valueOf(100.00));

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isFalse(); // Old payments don't count
    }

    @Test
    @DisplayName("Should handle mixed recent and old payments correctly")
    void testAnalyzeTransaction_MixedPayments_DetectsOnlyRecent() {
        // Given
        List<Payment> recentPayments = new ArrayList<>(createRecentPayments(8)); // 8 recent
        List<Payment> oldPayments = createOldPayments(20); // 20 old
        recentPayments.addAll(oldPayments);
        
        when(paymentRepository.findByFromAccountIdAndStatus(testAccountId, PaymentStatus.COMPLETED))
                .thenReturn(recentPayments);
        validPayment.setAmount(BigDecimal.valueOf(100.00));

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then
        assertThat(result.isFraud()).isFalse(); // Only 8 recent payments
    }

    @Test
    @DisplayName("FRAUD: Blacklist check takes priority over other checks")
    void testAnalyzeTransaction_BlacklistPriority_DetectsFraudFirst() {
        // Given - Blacklisted account with suspicious amount
        fraudDetectionService.addToBlacklist(testAccountId);
        validPayment.setAmount(BigDecimal.valueOf(50000.00));

        // When
        FraudDetectionService.FraudAnalysisResult result = 
                fraudDetectionService.analyzeTransaction(validPayment);

        // Then - Should detect blacklist BEFORE checking amount
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getFraudType()).isEqualTo(FraudType.BLACKLIST);
        
        // Verify amount check was not performed (short-circuit)
        verify(paymentRepository, never()).findByFromAccountIdAndStatus(anyLong(), any());
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Creates payments within the last hour
     */
    private List<Payment> createRecentPayments(int count) {
        Payment[] payments = new Payment[count];
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < count; i++) {
            payments[i] = Payment.builder()
                    .id((long) i)
                    .fromAccountId(testAccountId)
                    .toAccountId(200L)
                    .amount(BigDecimal.valueOf(100.00))
                    .currency("EUR")
                    .paymentType(PaymentType.STANDARD)
                    .status(PaymentStatus.COMPLETED)
                    .userId(10L)
                    .createdAt(now.minusMinutes(i * 5)) // Spread over last hour
                    .build();
        }
        
        return Arrays.asList(payments);
    }

    /**
     * Creates payments older than 1 hour
     */
    private List<Payment> createOldPayments(int count) {
        Payment[] payments = new Payment[count];
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        
        for (int i = 0; i < count; i++) {
            payments[i] = Payment.builder()
                    .id((long) (i + 1000))
                    .fromAccountId(testAccountId)
                    .toAccountId(200L)
                    .amount(BigDecimal.valueOf(100.00))
                    .currency("EUR")
                    .paymentType(PaymentType.STANDARD)
                    .status(PaymentStatus.COMPLETED)
                    .userId(10L)
                    .createdAt(twoHoursAgo.minusMinutes(i * 10)) // Old payments
                    .build();
        }
        
        return Arrays.asList(payments);
    }
}
