package com.banking.audit.repository.elasticsearch;

import com.banking.audit.model.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventElasticsearchRepository extends ElasticsearchRepository<AuditEvent, UUID> {

    // ===== Recherches par userId (Long) =====

    List<AuditEvent> findByUserId(Long userId);

    Page<AuditEvent> findByUserId(Long userId, Pageable pageable);

    List<AuditEvent> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<AuditEvent> findByUserIdOrderByTimestampDesc(Long userId);

    // ===== Recherches par type d'événement =====

    List<AuditEvent> findByEventType(AuditEvent.EventType eventType);

    List<AuditEvent> findByEventTypeAndTimestampBetween(AuditEvent.EventType eventType,
                                                        LocalDateTime start, LocalDateTime end);

    Page<AuditEvent> findByEventTypeAndTimestampBetween(AuditEvent.EventType eventType,
                                                        LocalDateTime start, LocalDateTime end,
                                                        Pageable pageable);

    // ===== Recherches par score de risque =====

    List<AuditEvent> findByRiskScoreGreaterThan(Double riskScore);

    List<AuditEvent> findByRiskScoreGreaterThanEqual(Double riskScore);

    Page<AuditEvent> findByRiskScoreGreaterThanEqual(Double riskScore, Pageable pageable);

    List<AuditEvent> findByUserIdAndRiskScoreGreaterThanEqual(Long userId, Double riskScore);

    // ===== Recherches par résultat =====

    List<AuditEvent> findByResult(AuditEvent.AuditResult result);

    Page<AuditEvent> findByResult(AuditEvent.AuditResult result, Pageable pageable);

    List<AuditEvent> findByUserIdAndResult(Long userId, AuditEvent.AuditResult result);

    // ===== Recherches par service source =====

    List<AuditEvent> findByServiceSource(String serviceSource);

    Page<AuditEvent> findByServiceSource(String serviceSource, Pageable pageable);

    // ===== Recherches par période =====

    List<AuditEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    Page<AuditEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<AuditEvent> findByTimestampAfter(LocalDateTime timestamp);

    // ===== Recherches combinées complexes =====

    List<AuditEvent> findByUserIdAndEventTypeAndTimestampBetween(Long userId,
                                                                 AuditEvent.EventType eventType,
                                                                 LocalDateTime start,
                                                                 LocalDateTime end);

    // ===== Recherches full-text avec Elasticsearch =====

    @Query("{\"bool\": {\"must\": [{\"match\": {\"action\": \"?0\"}}]}}")
    List<AuditEvent> searchByAction(String action);

    @Query("{\"bool\": {\"must\": [{\"term\": {\"userId\": ?0}}, {\"match\": {\"action\": \"?1\"}}]}}")
    List<AuditEvent> searchByUserIdAndAction(Long userId, String action);

    @Query("{\"bool\": {\"must\": [{\"range\": {\"timestamp\": {\"gte\": \"?0\", \"lte\": \"?1\"}}}]}}")
    List<AuditEvent> searchByDateRange(LocalDateTime start, LocalDateTime end);

    // ===== Recherches par IP et device =====

    List<AuditEvent> findByIpAddress(String ipAddress);

    List<AuditEvent> findByDeviceId(String deviceId);

    List<AuditEvent> findByUserIdAndIpAddress(Long userId, String ipAddress);

    // ===== Recherches pour détection d'anomalies =====

    @Query("{\"bool\": {\"must\": [{\"term\": {\"userId\": ?0}}, {\"term\": {\"result\": \"FAILURE\"}}, {\"range\": {\"timestamp\": {\"gte\": \"?1\"}}}]}}")
    List<AuditEvent> findFailedAttemptsByUserSince(Long userId, LocalDateTime since);

    @Query("{\"bool\": {\"must\": [{\"range\": {\"riskScore\": {\"gte\": ?0}}}, {\"range\": {\"timestamp\": {\"gte\": \"?1\"}}}]}}")
    List<AuditEvent> findHighRiskEventsSince(Double minRiskScore, LocalDateTime since);

    // ===== Recherches pour compliance =====

    @Query("{\"bool\": {\"must\": [{\"term\": {\"complianceFlags\": \"?0\"}}]}}")
    List<AuditEvent> findByComplianceFlag(String flag);

    @Query("{\"bool\": {\"must\": [{\"term\": {\"userId\": ?0}}, {\"term\": {\"complianceFlags\": \"?1\"}}]}}")
    List<AuditEvent> findByUserIdAndComplianceFlag(Long userId, String flag);

    // ===== Recherches avancées multi-critères =====

    @Query("{\"bool\": {\"must\": [{\"term\": {\"userId\": ?0}}, {\"range\": {\"timestamp\": {\"gte\": \"?1\", \"lte\": \"?2\"}}}, {\"range\": {\"riskScore\": {\"gte\": ?3}}}]}}")
    List<AuditEvent> findHighRiskEventsByUserInPeriod(Long userId, LocalDateTime start,
                                                      LocalDateTime end, Double minRiskScore);

    // ===== Agrégations et statistiques =====

    long countByUserId(Long userId);

    long countByEventType(AuditEvent.EventType eventType);

    long countByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    long countByRiskScoreGreaterThanEqual(Double riskScore);

    // ===== Recherches par session =====

    List<AuditEvent> findBySessionId(String sessionId);

    List<AuditEvent> findByUserIdAndSessionId(Long userId, String sessionId);

    // ===== Recherches géographiques =====

    List<AuditEvent> findByGeolocation(String geolocation);

    List<AuditEvent> findByUserIdAndGeolocation(Long userId, String geolocation);

    @Query("{\"bool\": {\"must\": [{\"term\": {\"userId\": ?0}}], \"must_not\": [{\"term\": {\"geolocation\": \"?1\"}}]}}")
    List<AuditEvent> findByUserIdAndGeolocationNot(Long userId, String geolocation);
}