package com.banking.audit.service;

import com.banking.audit.model.AuditEvent;
import com.banking.audit.repository.AuditEventRepository;
import com.banking.audit.repository.elasticsearch.AuditEventElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final AuditEventElasticsearchRepository elasticsearchRepository;

    @Transactional
    public AuditEvent saveAuditEvent(AuditEvent event) {
        try {
            // Calculate checksum for integrity
            event.setChecksum(calculateChecksum(event));

            // Set retention date based on compliance requirements
            event.setRetentionUntil(calculateRetentionDate(event));

            // Save to PostgreSQL (hot storage)
            AuditEvent saved = auditEventRepository.save(event);

            // Asynchronously index to Elasticsearch (warm storage)
            try {
                elasticsearchRepository.save(saved);
            } catch (Exception e) {
                log.warn("Failed to index audit event to Elasticsearch: {}", e.getMessage());
                // Don't fail the main operation if ES indexing fails
            }

            log.info("Audit event saved: {} - {} - User: {}",
                    saved.getEventId(), saved.getEventType(), saved.getUserId());

            return saved;

        } catch (Exception e) {
            log.error("Error saving audit event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save audit event", e);
        }
    }

    public Page<AuditEvent> findAllEvents(Pageable pageable) {
        return auditEventRepository.findAll(pageable);
    }

    // ✅ CORRIGÉ: UUID au lieu de String
    public Optional<AuditEvent> findEventById(UUID eventId) {
        return auditEventRepository.findById(eventId);
    }

    public Page<AuditEvent> findEventsByUserId(Long userId, Pageable pageable) {
        return auditEventRepository.findByUserId(userId, pageable);
    }

    public Page<AuditEvent> findEventsByEventType(AuditEvent.EventType eventType, Pageable pageable) {
        return auditEventRepository.findByEventType(eventType, pageable);
    }

    public Page<AuditEvent> findEventsByServiceSource(String serviceSource, Pageable pageable) {
        return auditEventRepository.findByServiceSource(serviceSource, pageable);
    }

    public Page<AuditEvent> findEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditEventRepository.findByTimestampBetween(startDate, endDate, pageable);
    }

    public List<AuditEvent> findUserTimeline(Long userId) {
        return auditEventRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public Page<AuditEvent> searchEvents(Long userId,
                                         AuditEvent.EventType eventType,
                                         LocalDateTime startDate,
                                         LocalDateTime endDate,
                                         Pageable pageable) {
        if (userId != null && eventType != null && startDate != null && endDate != null) {
            return auditEventRepository.findByUserIdAndEventTypeAndTimestampBetween(
                    userId, eventType, startDate, endDate, pageable);
        } else if (userId != null && startDate != null && endDate != null) {
            return auditEventRepository.findByUserIdAndTimestampBetween(
                    userId, startDate, endDate, pageable);
        } else if (eventType != null && startDate != null && endDate != null) {
            return auditEventRepository.findByEventTypeAndTimestampBetween(
                    eventType, startDate, endDate, pageable);
        } else if (startDate != null && endDate != null) {
            return findEventsByDateRange(startDate, endDate, pageable);
        } else if (userId != null) {
            return findEventsByUserId(userId, pageable);
        } else if (eventType != null) {
            return findEventsByEventType(eventType, pageable);
        } else {
            return findAllEvents(pageable);
        }
    }

    public Page<AuditEvent> findHighRiskEvents(Double minRiskScore, Pageable pageable) {
        return auditEventRepository.findByRiskScoreGreaterThanEqual(minRiskScore, pageable);
    }

    public Page<AuditEvent> findFailedEvents(Pageable pageable) {
        return auditEventRepository.findByResult(AuditEvent.AuditResult.FAILURE, pageable);
    }

    public long countEventsByUserId(Long userId) {
        return auditEventRepository.countByUserId(userId);
    }

    public long countEventsByType(AuditEvent.EventType eventType) {
        return auditEventRepository.countByEventType(eventType);
    }

    public Map<String, Object> getUserActivitySummary(Long userId, LocalDateTime since) {
        List<AuditEvent> events = auditEventRepository.findByUserIdAndTimestampAfter(userId, since);

        Map<String, Object> summary = new HashMap<>();
        summary.put("userId", userId);
        summary.put("period", Map.of("from", since, "to", LocalDateTime.now()));
        summary.put("totalEvents", events.size());

        // Count by event type
        Map<String, Long> eventTypeCounts = new HashMap<>();
        events.forEach(event -> {
            String type = event.getEventType().name();
            eventTypeCounts.put(type, eventTypeCounts.getOrDefault(type, 0L) + 1);
        });
        summary.put("eventsByType", eventTypeCounts);

        // Count by result
        long successCount = events.stream()
                .filter(e -> e.getResult() == AuditEvent.AuditResult.SUCCESS)
                .count();
        long failureCount = events.stream()
                .filter(e -> e.getResult() == AuditEvent.AuditResult.FAILURE)
                .count();

        summary.put("successCount", successCount);
        summary.put("failureCount", failureCount);
        summary.put("successRate", events.isEmpty() ? 100.0 : (successCount * 100.0 / events.size()));

        // High risk events
        long highRiskCount = events.stream()
                .filter(e -> e.getRiskScore() != null && e.getRiskScore() >= 0.7)
                .count();
        summary.put("highRiskEvents", highRiskCount);

        // Last activity
        summary.put("lastActivity", events.isEmpty() ? null : events.get(0).getTimestamp());

        return summary;
    }

    @Transactional
    public void archiveOldEvents(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        List<AuditEvent> oldEvents = auditEventRepository.findByTimestampBefore(cutoffDate);

        log.info("Archiving {} events older than {}", oldEvents.size(), cutoffDate);

        // Here you would implement S3/cold storage logic
        // For now, we just delete from hot storage
        auditEventRepository.deleteAll(oldEvents);
    }

    @Transactional
    public void anonymizeUserData(Long userId) {
        List<AuditEvent> userEvents = auditEventRepository.findByUserId(userId);

        userEvents.forEach(event -> {
            event.setUserId(-1L); // ✅ CORRIGÉ: -1L au lieu de String
            event.setIpAddress("XXX.XXX.XXX.XXX");
            event.setUserAgent("ANONYMIZED");
            event.setDeviceId("ANONYMIZED");

            // ✅ CORRIGÉ: Utilise metadata qui existe maintenant
            if (event.getMetadata() != null) {
                event.getMetadata().put("anonymized", "true");
                event.getMetadata().put("anonymizedAt", LocalDateTime.now().toString());
                event.getMetadata().put("originalUserId", userId.toString());
            }
        });

        auditEventRepository.saveAll(userEvents);
        log.info("Anonymized {} audit events for user {}", userEvents.size(), userId);
    }

    public boolean verifyIntegrity(AuditEvent event) {
        String currentChecksum = event.getChecksum();
        String calculatedChecksum = calculateChecksum(event);
        boolean isValid = currentChecksum.equals(calculatedChecksum);

        if (!isValid) {
            log.warn("Integrity check failed for event: {} - Expected: {}, Got: {}",
                    event.getEventId(), currentChecksum, calculatedChecksum);
        }

        return isValid;
    }

    // Private helper methods

    private String calculateChecksum(AuditEvent event) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            StringBuilder data = new StringBuilder();
            data.append(event.getTimestamp())
                    .append(event.getEventType())
                    .append(event.getUserId())
                    .append(event.getServiceSource())
                    .append(event.getAction())
                    .append(event.getResourceId())
                    .append(event.getResult());

            byte[] hash = digest.digest(data.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            return "CHECKSUM_ERROR";
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private LocalDateTime calculateRetentionDate(AuditEvent event) {
        // Default retention: 10 years for compliance
        int retentionYears = 10;

        // Adjust based on event sensitivity
        if (event.getComplianceFlags() != null) {
            if (event.getComplianceFlags().contains("GDPR")) {
                retentionYears = 3; // GDPR minimum
            }
            if (event.getComplianceFlags().contains("RGPD")) {
                retentionYears = 3; // RGPD is French name for GDPR
            }
            if (event.getComplianceFlags().contains("FINANCIAL")) {
                retentionYears = 10; // Financial records
            }
            if (event.getComplianceFlags().contains("PCI_DSS")) {
                retentionYears = 1; // PCI DSS requirement
            }
        }

        return LocalDateTime.now().plusYears(retentionYears);
    }
}