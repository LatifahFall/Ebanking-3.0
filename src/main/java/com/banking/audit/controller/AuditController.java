package com.banking.audit.controller;

import com.banking.audit.model.AuditEvent;
import com.banking.audit.service.AuditService;
import com.banking.audit.service.AuditExportService;
import com.banking.audit.service.AuditMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditController {

    private final AuditService auditService;
    private final AuditExportService exportService;
    private final AuditMonitoringService monitoringService;

    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<AuditEvent> createAuditEvent(@RequestBody AuditEvent event) {
        log.info("Creating audit event: {}", event.getEventType());
        AuditEvent saved = auditService.saveAuditEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Page<AuditEvent>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AuditEvent> events = auditService.findAllEvents(pageable);
        return ResponseEntity.ok(events);
    }

    // ✅ CORRIGÉ: UUID au lieu de String
    @GetMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<AuditEvent> getEventById(@PathVariable UUID eventId) {
        return auditService.findEventById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/events/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Page<AuditEvent>> searchEvents(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) AuditEvent.EventType eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<AuditEvent> events = auditService.searchEvents(userId, eventType, startDate, endDate, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/users/{userId}/timeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT') or #userId == authentication.principal.id")
    public ResponseEntity<List<AuditEvent>> getUserTimeline(
            @PathVariable Long userId,
            Authentication authentication) {

        log.info("Fetching timeline for user: {}", userId);
        List<AuditEvent> timeline = auditService.findUserTimeline(userId);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/users/{userId}/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT') or #userId == authentication.principal.id")
    public ResponseEntity<Page<AuditEvent>> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        log.info("Fetching events for user: {} by {}", userId, authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditEvent> events = auditService.findEventsByUserId(userId, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/type/{eventType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Page<AuditEvent>> getEventsByType(
            @PathVariable AuditEvent.EventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditEvent> events = auditService.findEventsByEventType(eventType, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/service/{serviceSource}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditEvent>> getEventsByService(
            @PathVariable String serviceSource,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditEvent> events = auditService.findEventsByServiceSource(serviceSource, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/high-risk")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Page<AuditEvent>> getHighRiskEvents(
            @RequestParam(defaultValue = "0.7") Double minRiskScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "riskScore"));
        Page<AuditEvent> events = auditService.findHighRiskEvents(minRiskScore, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/failures")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Page<AuditEvent>> getFailedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditEvent> events = auditService.findFailedEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/stats/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT') or #userId == authentication.principal.id")
    public ResponseEntity<Map<String, Long>> getUserStats(
            @PathVariable Long userId,
            Authentication authentication) {

        log.info("Fetching stats for user: {}", userId);
        long count = auditService.countEventsByUserId(userId);
        return ResponseEntity.ok(Map.of("totalEvents", count));
    }

    @GetMapping("/stats/type/{eventType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Map<String, Long>> getTypeStats(@PathVariable AuditEvent.EventType eventType) {
        long count = auditService.countEventsByType(eventType);
        return ResponseEntity.ok(Map.of("totalEvents", count));
    }

    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> exportAuditReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "PDF") String format,
            Authentication authentication) {

        log.info("Export request by {}: {} from {} to {}",
                authentication.getName(), format, startDate, endDate);

        String jobId = exportService.initiateExport(startDate, endDate, format);

        return ResponseEntity.accepted()
                .body(Map.of("jobId", jobId, "status", "PROCESSING"));
    }

    @GetMapping("/export/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getExportFile(@PathVariable String jobId) {
        // Implementation would retrieve from S3/storage
        // For now, return not implemented
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/compliance/report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getComplianceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "GDPR") String regulation) {

        log.info("Generating compliance report for {} from {} to {}", regulation, startDate, endDate);

        // Simplified compliance report
        Pageable pageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditEvent> events = auditService.findEventsByDateRange(startDate, endDate, pageable);

        long totalEvents = events.getTotalElements();
        long failedEvents = events.stream()
                .filter(e -> e.getResult() == AuditEvent.AuditResult.FAILURE)
                .count();

        return ResponseEntity.ok(Map.of(
                "regulation", regulation,
                "period", Map.of("start", startDate, "end", endDate),
                "totalEvents", totalEvents,
                "failedEvents", failedEvents,
                "complianceRate", totalEvents > 0 ? (double)(totalEvents - failedEvents) / totalEvents * 100 : 100
        ));
    }

    @PostMapping("/users/{userId}/anonymize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> anonymizeUserData(
            @PathVariable Long userId,
            Authentication authentication) {

        log.warn("Anonymizing audit data for user: {} by admin: {}", userId, authentication.getName());
        auditService.anonymizeUserData(userId);
        return ResponseEntity.ok(Map.of(
                "message", "User data anonymized successfully",
                "userId", userId.toString()
        ));
    }

    // ✅ CORRIGÉ: UUID au lieu de String
    @PostMapping("/verify/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Map<String, Boolean>> verifyEventIntegrity(@PathVariable UUID eventId) {
        return auditService.findEventById(eventId)
                .map(event -> {
                    boolean valid = auditService.verifyIntegrity(event);
                    return ResponseEntity.ok(Map.of("valid", valid));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}/activity-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT') or #userId == authentication.principal.id")
    public ResponseEntity<Map<String, Object>> getUserActivitySummary(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Object> summary = auditService.getUserActivitySummary(userId, since);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/my/events")
    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    public ResponseEntity<Page<AuditEvent>> getMyEvents(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = (Long) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditEvent> events = auditService.findEventsByUserId(userId, pageable);

        return ResponseEntity.ok(events);
    }

    // ========================================
    // ENDPOINTS DE MONITORING (Nouveaux)
    // ========================================

    @GetMapping("/users/{userId}/suspicious")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<AuditEvent>> getSuspiciousActivity(@PathVariable Long userId) {
        List<AuditEvent> suspicious = monitoringService.detectSuspiciousActivity(userId);
        return ResponseEntity.ok(suspicious);
    }

    @GetMapping("/users/{userId}/security-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Map<String, Object>> getSecurityReport(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> report = monitoringService.generateSecurityReport(userId, days);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/users/{userId}/multiple-ip-check")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Map<String, Object>> checkMultipleIpLogins(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "24") int hours) {
        Map<String, Object> result = monitoringService.detectMultipleIpLogins(userId, hours);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/monitoring/abnormal-activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAbnormalActivity(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "100") long threshold) {
        List<Map<String, Object>> users = monitoringService.detectAbnormalActivity(hours, threshold);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/compliance/{flag}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getComplianceAudit(
            @PathVariable String flag,
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> audit = monitoringService.getComplianceAudit(flag, days);
        return ResponseEntity.ok(audit);
    }

    @GetMapping("/search/action")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<AuditEvent>> searchByAction(
            @RequestParam String action) {
        List<AuditEvent> events = monitoringService.searchByAction(action);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/users/{userId}/risk-analysis")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Map<String, Object>> getRealTimeRiskAnalysis(@PathVariable Long userId) {
        Map<String, Object> analysis = monitoringService.getRealTimeRiskAnalysis(userId);
        return ResponseEntity.ok(analysis);
    }
}