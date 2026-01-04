package com.banking.audit.repository;

import com.banking.audit.model.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    // ===== Recherche par userId (Long) =====

    Page<AuditEvent> findByUserId(Long userId, Pageable pageable);

    List<AuditEvent> findByUserId(Long userId);

    List<AuditEvent> findByUserIdOrderByTimestampDesc(Long userId);

    List<AuditEvent> findByUserIdAndTimestampAfter(Long userId, LocalDateTime timestamp);

    // ===== Recherche par type d'événement =====

    Page<AuditEvent> findByEventType(AuditEvent.EventType eventType, Pageable pageable);

    // ===== Recherche par service source =====

    Page<AuditEvent> findByServiceSource(String serviceSource, Pageable pageable);

    // ===== Recherche par période =====

    Page<AuditEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<AuditEvent> findByTimestampBefore(LocalDateTime timestamp);

    List<AuditEvent> findByTimestampAfter(LocalDateTime timestamp);

    // ===== Recherches combinées avec userId =====

    Page<AuditEvent> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start,
                                                     LocalDateTime end, Pageable pageable);

    Page<AuditEvent> findByUserIdAndEventType(Long userId, AuditEvent.EventType eventType,
                                              Pageable pageable);

    Page<AuditEvent> findByUserIdAndEventTypeAndTimestampBetween(Long userId,
                                                                 AuditEvent.EventType eventType,
                                                                 LocalDateTime start,
                                                                 LocalDateTime end,
                                                                 Pageable pageable);

    // ===== Recherches par type d'événement et période =====

    Page<AuditEvent> findByEventTypeAndTimestampBetween(AuditEvent.EventType eventType,
                                                        LocalDateTime start, LocalDateTime end,
                                                        Pageable pageable);

    // ===== Recherche par score de risque =====

    Page<AuditEvent> findByRiskScoreGreaterThanEqual(Double minRiskScore, Pageable pageable);

    Page<AuditEvent> findByRiskScoreBetween(Double minScore, Double maxScore, Pageable pageable);

    List<AuditEvent> findByUserIdAndRiskScoreGreaterThanEqual(Long userId, Double minRiskScore);

    // ===== Recherche par résultat =====

    Page<AuditEvent> findByResult(AuditEvent.AuditResult result, Pageable pageable);

    Page<AuditEvent> findByUserIdAndResult(Long userId, AuditEvent.AuditResult result,
                                           Pageable pageable);

    // ===== Compteurs =====

    long countByUserId(Long userId);

    long countByEventType(AuditEvent.EventType eventType);

    long countByUserIdAndEventType(Long userId, AuditEvent.EventType eventType);

    long countByResult(AuditEvent.AuditResult result);

    long countByUserIdAndResult(Long userId, AuditEvent.AuditResult result);

    long countByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // ===== Vérifications =====

    boolean existsByChecksum(String checksum);

    boolean existsByUserId(Long userId);

    // ===== Requêtes personnalisées pour statistiques =====

    @Query("SELECT e.eventType, COUNT(e) FROM AuditEvent e WHERE e.userId = :userId GROUP BY e.eventType")
    List<Object[]> countEventsByTypeForUser(@Param("userId") Long userId);

    @Query("SELECT DATE(e.timestamp), COUNT(e) FROM AuditEvent e WHERE e.userId = :userId " +
            "AND e.timestamp BETWEEN :start AND :end GROUP BY DATE(e.timestamp) ORDER BY DATE(e.timestamp)")
    List<Object[]> getDailyActivityForUser(@Param("userId") Long userId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("SELECT e.result, COUNT(e) FROM AuditEvent e WHERE e.userId = :userId " +
            "AND e.timestamp >= :since GROUP BY e.result")
    List<Object[]> countResultsByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT AVG(e.riskScore) FROM AuditEvent e WHERE e.userId = :userId " +
            "AND e.riskScore IS NOT NULL AND e.timestamp >= :since")
    Double getAverageRiskScoreForUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // ===== Recherches pour conformité et compliance =====

    @Query("SELECT e FROM AuditEvent e WHERE :flag MEMBER OF e.complianceFlags")
    Page<AuditEvent> findByComplianceFlag(@Param("flag") String flag, Pageable pageable);

    @Query("SELECT e FROM AuditEvent e WHERE e.userId = :userId AND :flag MEMBER OF e.complianceFlags")
    List<AuditEvent> findByUserIdAndComplianceFlag(@Param("userId") Long userId,
                                                   @Param("flag") String flag);

    @Query("SELECT COUNT(e) FROM AuditEvent e WHERE e.retentionUntil < :date")
    long countEventsToArchive(@Param("date") LocalDateTime date);

    // ===== Recherches pour détection d'anomalies =====

    @Query("SELECT e FROM AuditEvent e WHERE e.userId = :userId " +
            "AND e.result = 'FAILURE' AND e.timestamp >= :since ORDER BY e.timestamp DESC")
    List<AuditEvent> findRecentFailuresForUser(@Param("userId") Long userId,
                                               @Param("since") LocalDateTime since);

    @Query("SELECT e.userId, COUNT(e) FROM AuditEvent e WHERE e.timestamp >= :since " +
            "GROUP BY e.userId HAVING COUNT(e) > :threshold")
    List<Object[]> findUsersWithHighActivity(@Param("since") LocalDateTime since,
                                             @Param("threshold") Long threshold);
}