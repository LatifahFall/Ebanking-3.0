package com.banking.audit.service;

import com.banking.audit.model.AuditEvent;
import com.banking.audit.repository.AuditEventRepository;
import com.banking.audit.repository.elasticsearch.AuditEventElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditMonitoringService {

    private final AuditEventRepository auditEventRepository;
    private final AuditEventElasticsearchRepository elasticsearchRepository;

    /**
     * Détecte les activités suspectes pour un utilisateur
     */
    public List<AuditEvent> detectSuspiciousActivity(Long userId) {
        log.info("Detecting suspicious activity for user: {}", userId);

        LocalDateTime since = LocalDateTime.now().minusHours(24);

        // Recherche dans Elasticsearch (plus rapide)
        List<AuditEvent> recentFailures = elasticsearchRepository
                .findFailedAttemptsByUserSince(userId, since);

        // Filtre les événements à haut risque
        List<AuditEvent> suspicious = recentFailures.stream()
                .filter(e -> e.getRiskScore() != null && e.getRiskScore() >= 0.7)
                .collect(Collectors.toList());

        if (!suspicious.isEmpty()) {
            log.warn("Found {} suspicious events for user {}", suspicious.size(), userId);
        }

        return suspicious;
    }

    /**
     * Génère un rapport de sécurité pour un utilisateur
     */
    public Map<String, Object> generateSecurityReport(Long userId, int days) {
        log.info("Generating security report for user {} over {} days", userId, days);

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> report = new HashMap<>();
        report.put("userId", userId);
        report.put("period", Map.of("from", since, "to", now));

        // Événements à haut risque
        List<AuditEvent> highRisk = elasticsearchRepository
                .findByUserIdAndRiskScoreGreaterThanEqual(userId, 0.7);
        report.put("highRiskEventsCount", highRisk.size());

        // Tentatives échouées
        List<AuditEvent> failures = elasticsearchRepository
                .findFailedAttemptsByUserSince(userId, since);
        report.put("failedAttemptsCount", failures.size());

        // Distribution par type d'événement
        List<Object[]> eventsByType = auditEventRepository
                .countEventsByTypeForUser(userId);
        Map<String, Long> typeDistribution = new HashMap<>();
        for (Object[] row : eventsByType) {
            typeDistribution.put(row[0].toString(), (Long) row[1]);
        }
        report.put("eventsByType", typeDistribution);

        // Score de risque moyen
        Double avgRiskScore = auditEventRepository
                .getAverageRiskScoreForUser(userId, since);
        report.put("averageRiskScore", avgRiskScore != null ? avgRiskScore : 0.0);

        // Activité quotidienne
        List<Object[]> dailyActivity = auditEventRepository
                .getDailyActivityForUser(userId, since, now);
        List<Map<String, Object>> activityTimeline = dailyActivity.stream()
                .map(row -> Map.of(
                        "date", row[0].toString(),
                        "count", row[1]
                ))
                .collect(Collectors.toList());
        report.put("dailyActivity", activityTimeline);

        // Résultats (succès vs échecs)
        List<Object[]> results = auditEventRepository
                .countResultsByUser(userId, since);
        Map<String, Long> resultDistribution = new HashMap<>();
        for (Object[] row : results) {
            resultDistribution.put(row[0].toString(), (Long) row[1]);
        }
        report.put("resultsByType", resultDistribution);

        return report;
    }

    /**
     * Détecte les connexions depuis plusieurs IPs
     */
    public Map<String, Object> detectMultipleIpLogins(Long userId, int hours) {
        log.info("Checking for multiple IP logins for user {} in last {} hours", userId, hours);

        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<AuditEvent> loginEvents = elasticsearchRepository
                .findByUserIdAndTimestampBetween(userId, since, LocalDateTime.now())
                .stream()
                .filter(e -> e.getEventType() == AuditEvent.EventType.LOGIN
                        || e.getEventType() == AuditEvent.EventType.AUTHENTICATION)
                .collect(Collectors.toList());

        // Grouper par IP
        Map<String, Long> ipCounts = loginEvents.stream()
                .filter(e -> e.getIpAddress() != null)
                .collect(Collectors.groupingBy(
                        AuditEvent::getIpAddress,
                        Collectors.counting()
                ));

        boolean suspicious = ipCounts.size() > 3; // Plus de 3 IPs différentes = suspect

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("period", hours + " hours");
        result.put("uniqueIps", ipCounts.size());
        result.put("ipDistribution", ipCounts);
        result.put("suspicious", suspicious);

        if (suspicious) {
            log.warn("User {} logged in from {} different IPs in {} hours",
                    userId, ipCounts.size(), hours);
        }

        return result;
    }

    /**
     * Détecte les utilisateurs avec activité anormalement élevée
     */
    public List<Map<String, Object>> detectAbnormalActivity(int hours, long threshold) {
        log.info("Detecting users with high activity (threshold: {} events in {} hours)",
                threshold, hours);

        LocalDateTime since = LocalDateTime.now().minusHours(hours);

        List<Object[]> highActivityUsers = auditEventRepository
                .findUsersWithHighActivity(since, threshold);

        return highActivityUsers.stream()
                .map(row -> Map.of(
                        "userId", row[0],
                        "eventCount", row[1]
                ))
                .collect(Collectors.toList());
    }

    /**
     * Recherche d'événements par compliance flag
     */
    public Map<String, Object> getComplianceAudit(String flag, int days) {
        log.info("Generating compliance audit for flag: {}", flag);

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        // Recherche dans Elasticsearch
        List<AuditEvent> events = elasticsearchRepository.findByComplianceFlag(flag);

        // Filtre par période
        List<AuditEvent> recentEvents = events.stream()
                .filter(e -> e.getTimestamp().isAfter(since))
                .collect(Collectors.toList());

        Map<String, Object> audit = new HashMap<>();
        audit.put("complianceFlag", flag);
        audit.put("totalEvents", recentEvents.size());
        audit.put("period", days + " days");

        // Grouper par type
        Map<String, Long> byType = recentEvents.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEventType().name(),
                        Collectors.counting()
                ));
        audit.put("eventsByType", byType);

        // Compter les échecs
        long failures = recentEvents.stream()
                .filter(e -> e.getResult() == AuditEvent.AuditResult.FAILURE)
                .count();
        audit.put("failures", failures);

        return audit;
    }

    /**
     * Recherche full-text dans les actions
     */
    public List<AuditEvent> searchByAction(String action) {
        log.info("Searching events by action: {}", action);
        return elasticsearchRepository.searchByAction(action);
    }

    /**
     * Analyse de risque en temps réel
     */
    public Map<String, Object> getRealTimeRiskAnalysis(Long userId) {
        log.info("Performing real-time risk analysis for user: {}", userId);

        LocalDateTime lastHour = LocalDateTime.now().minusHours(1);

        // Événements récents à haut risque
        List<AuditEvent> highRisk = elasticsearchRepository
                .findHighRiskEventsByUserInPeriod(userId, lastHour, LocalDateTime.now(), 0.7);

        // Échecs récents
        List<AuditEvent> recentFailures = elasticsearchRepository
                .findFailedAttemptsByUserSince(userId, lastHour);

        // Calculer le score de risque global
        double riskScore = calculateOverallRiskScore(highRisk, recentFailures);
        String riskLevel = getRiskLevel(riskScore);

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("userId", userId);
        analysis.put("timestamp", LocalDateTime.now());
        analysis.put("riskScore", riskScore);
        analysis.put("riskLevel", riskLevel);
        analysis.put("highRiskEventsLastHour", highRisk.size());
        analysis.put("failedAttemptsLastHour", recentFailures.size());

        if ("HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel)) {
            log.warn("HIGH RISK detected for user {}: score={}", userId, riskScore);
        }

        return analysis;
    }

    // Méthodes privées utilitaires

    private double calculateOverallRiskScore(List<AuditEvent> highRisk, List<AuditEvent> failures) {
        double baseScore = 0.0;

        // Points pour événements à haut risque
        baseScore += highRisk.size() * 0.15;

        // Points pour échecs
        baseScore += failures.size() * 0.10;

        return Math.min(baseScore, 1.0); // Cap à V11__create_audit_partitioning.sql.0
    }

    private String getRiskLevel(double score) {
        if (score >= 0.9) return "CRITICAL";
        if (score >= 0.7) return "HIGH";
        if (score >= 0.5) return "MEDIUM";
        if (score >= 0.3) return "LOW";
        return "MINIMAL";
    }
}