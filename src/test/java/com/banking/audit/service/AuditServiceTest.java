// ========================================
// AuditServiceTest.java
// ========================================
package com.banking.audit.service;

import com.banking.audit.BaseIntegrationTest;
import com.banking.audit.model.AuditEvent;
import com.banking.audit.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuditServiceTest extends BaseIntegrationTest {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditEventRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should save audit event with checksum")
    void shouldSaveAuditEventWithChecksum() {
        // Given
        AuditEvent event = createTestAuditEvent(123L, AuditEvent.EventType.USER_LOGIN);

        // When
        AuditEvent saved = auditService.saveAuditEvent(event);

        // Then
        assertThat(saved.getEventId()).isNotNull();
        assertThat(saved.getChecksum()).isNotNull();
        assertThat(saved.getChecksum()).hasSize(64); // SHA-256 hex length
        assertThat(saved.getRetentionUntil()).isNotNull();
    }

    @Test
    @DisplayName("Should find events by user ID")
    void shouldFindEventsByUserId() {
        // Given
        Long userId = 100L;
        auditService.saveAuditEvent(createTestAuditEvent(userId, AuditEvent.EventType.USER_LOGIN));
        auditService.saveAuditEvent(createTestAuditEvent(userId, AuditEvent.EventType.ACCOUNT_CREATED));
        auditService.saveAuditEvent(createTestAuditEvent(200L, AuditEvent.EventType.USER_LOGIN));

        // When
        Page<AuditEvent> events = auditService.findEventsByUserId(userId, PageRequest.of(0, 10));

        // Then
        assertThat(events.getTotalElements()).isEqualTo(2);
        assertThat(events.getContent()).allMatch(e -> e.getUserId().equals(userId));
    }

    @Test
    @DisplayName("Should find events by type")
    void shouldFindEventsByType() {
        // Given
        auditService.saveAuditEvent(createTestAuditEvent(1L, AuditEvent.EventType.USER_LOGIN));
        auditService.saveAuditEvent(createTestAuditEvent(2L, AuditEvent.EventType.USER_LOGIN));
        auditService.saveAuditEvent(createTestAuditEvent(3L, AuditEvent.EventType.PAYMENT_COMPLETED));

        // When
        Page<AuditEvent> events = auditService.findEventsByEventType(
                AuditEvent.EventType.USER_LOGIN, PageRequest.of(0, 10));

        // Then
        assertThat(events.getTotalElements()).isEqualTo(2);
        assertThat(events.getContent()).allMatch(e -> e.getEventType() == AuditEvent.EventType.USER_LOGIN);
    }

    @Test
    @DisplayName("Should find events by date range")
    void shouldFindEventsByDateRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);

        auditService.saveAuditEvent(createTestAuditEvent(1L, AuditEvent.EventType.USER_LOGIN));

        // When
        Page<AuditEvent> events = auditService.findEventsByDateRange(
                yesterday, tomorrow, PageRequest.of(0, 10));

        // Then
        assertThat(events.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find high risk events")
    void shouldFindHighRiskEvents() {
        // Given
        AuditEvent highRisk = createTestAuditEvent(1L, AuditEvent.EventType.FRAUD_DETECTED);
        highRisk.setRiskScore(0.9);
        auditService.saveAuditEvent(highRisk);

        AuditEvent lowRisk = createTestAuditEvent(2L, AuditEvent.EventType.USER_LOGIN);
        lowRisk.setRiskScore(0.2);
        auditService.saveAuditEvent(lowRisk);

        // When
        Page<AuditEvent> events = auditService.findHighRiskEvents(0.7, PageRequest.of(0, 10));

        // Then
        assertThat(events.getTotalElements()).isEqualTo(1);
        assertThat(events.getContent().get(0).getRiskScore()).isGreaterThanOrEqualTo(0.7);
    }

    @Test
    @DisplayName("Should verify event integrity")
    void shouldVerifyEventIntegrity() {
        // Given
        AuditEvent event = createTestAuditEvent(1L, AuditEvent.EventType.USER_LOGIN);
        AuditEvent saved = auditService.saveAuditEvent(event);

        // When
        boolean isValid = auditService.verifyIntegrity(saved);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should detect tampered event")
    void shouldDetectTamperedEvent() {
        // Given
        AuditEvent event = createTestAuditEvent(1L, AuditEvent.EventType.USER_LOGIN);
        AuditEvent saved = auditService.saveAuditEvent(event);

        // Tamper with the event
        saved.setAction("TAMPERED_ACTION");

        // When
        boolean isValid = auditService.verifyIntegrity(saved);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should anonymize user data")
    void shouldAnonymizeUserData() {
        // Given
        Long userId = 999L;
        AuditEvent event = createTestAuditEvent(userId, AuditEvent.EventType.USER_LOGIN);
        event.setIpAddress("192.168.0.1");
        event.setDeviceId("device123");
        auditService.saveAuditEvent(event);

        // When
        auditService.anonymizeUserData(userId);

        // Then
        Page<AuditEvent> events = auditService.findEventsByUserId(userId, PageRequest.of(0, 10));
        assertThat(events.getTotalElements()).isEqualTo(0); // User ID has been anonymized
    }

    @Test
    @DisplayName("Should count events by user")
    void shouldCountEventsByUser() {
        // Given
        Long userId = 100L;
        auditService.saveAuditEvent(createTestAuditEvent(userId, AuditEvent.EventType.USER_LOGIN));
        auditService.saveAuditEvent(createTestAuditEvent(userId, AuditEvent.EventType.ACCOUNT_ACCESSED));

        // When
        long count = auditService.countEventsByUserId(userId);

        // Then
        assertThat(count).isEqualTo(2);
    }

    private AuditEvent createTestAuditEvent(Long userId, AuditEvent.EventType eventType) {
        Set<String> complianceFlags = new HashSet<>();
        complianceFlags.add("RGPD");
        complianceFlags.add("PSD2");

        return AuditEvent.builder()
                .eventType(eventType)
                .userId(userId)
                .sessionId("session_" + System.currentTimeMillis())
                .serviceSource("test-service")
                .action(eventType.name())
                .resourceType("TestResource")
                .resourceId("resource_123")
                .ipAddress("127.0.0.1")
                .userAgent("TestAgent/1.0")
                .geolocation("Test Location")
                .deviceId("test_device")
                .result(AuditEvent.AuditResult.SUCCESS)
                .complianceFlags(complianceFlags)
                .riskScore(0.0)
                .sensitive(false)
                .build();
    }
}
