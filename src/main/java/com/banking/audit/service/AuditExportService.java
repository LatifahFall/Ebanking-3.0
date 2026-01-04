package com.banking.audit.service;

import com.banking.audit.model.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditExportService {

    private final AuditService auditService;

     private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.export.directory:./exports}")
    private String exportDirectory;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Démarre un export asynchrone et retourne immédiatement un jobId
     */
    public String initiateExport(LocalDateTime startDate, LocalDateTime endDate, String format) {
        String jobId = UUID.randomUUID().toString();
        String safeFormat = format != null ? format.toUpperCase() : "CSV";

        log.info("🔄 [EXPORT] Initiating export job: {} | Period: {} → {} | Format: {}",
                jobId, startDate, endDate, safeFormat);

        // Lancement en arrière-plan
        exportAsync(jobId, startDate, endDate, safeFormat);

        return jobId;
    }

    @Async
    public void exportAsync(String jobId, LocalDateTime startDate, LocalDateTime endDate, String format) {
        try {
            log.info("🔨 [EXPORT] Starting background export job: {}", jobId);

            // Créer le dossier d'export si inexistant
            Path exportPath = Paths.get(exportDirectory);
            Files.createDirectories(exportPath);

            String filename = String.format("audit-export_%s_%s.%s",
                    DATE_FORMAT.format(LocalDateTime.now()),
                    jobId.substring(0, 8),
                    format.toLowerCase());

            Path filePath = exportPath.resolve(filename);

            if ("PDF".equalsIgnoreCase(format)) {
                generatePdfReport(filePath.toString(), startDate, endDate, jobId);
            } else {
                generateCsvReport(filePath.toString(), startDate, endDate, jobId);
            }

            // Publier un événement Kafka (optionnel)
            publishExportCompletedEvent(jobId, filePath.toString(), format);

            log.info("✅ [EXPORT] Export job {} completed successfully: {}", jobId, filePath);

        } catch (Exception e) {
            log.error("❌ [EXPORT] Export job {} failed", jobId, e);
            publishExportFailedEvent(jobId, e.getMessage());
        }
    }

    private void generateCsvReport(String filePath, LocalDateTime start, LocalDateTime end, String jobId) throws IOException {
        try (FileWriter writer = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                     "Event ID", "Service Source", "Event Type", "User ID", "IP Address",
                     "Action", "Timestamp", "Result", "Risk Score", "Compliance Flags"
             ))) {

            int page = 0;
            int size = 1000;
            Page<AuditEvent> auditPage;
            long totalEvents = 0;

            do {
                Pageable pageable = PageRequest.of(page++, size);
                auditPage = auditService.findEventsByDateRange(start, end, pageable);

                for (AuditEvent event : auditPage.getContent()) {
                    csvPrinter.printRecord(
                            event.getEventId(),
                            event.getServiceSource(),
                            event.getEventType(),
                            event.getUserId(),
                            event.getIpAddress(),
                            event.getAction(),
                            event.getTimestamp().format(TIMESTAMP_FORMAT),
                            event.getResult(),
                            event.getRiskScore(),
                            event.getComplianceFlags() != null ?
                                    String.join(", ", event.getComplianceFlags()) : ""
                    );
                    totalEvents++;
                }
                csvPrinter.flush();
            } while (auditPage.hasNext());

            log.info("📄 [EXPORT] CSV generated: {} ({} events)", filePath, totalEvents);
        }
    }

    private void generatePdfReport(String filePath, LocalDateTime start, LocalDateTime end, String jobId) throws IOException {


        StringBuilder report = new StringBuilder();
        report.append("=".repeat(80)).append("\n");
        report.append("           RAPPORT D'AUDIT - E-BANKING 3.0\n");
        report.append("=".repeat(80)).append("\n\n");
        report.append("Période : ").append(start).append(" → ").append(end).append("\n");
        report.append("Généré le : ").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("\n");
        report.append("Job ID : ").append(jobId).append("\n\n");

        int page = 0;
        int size = 1000;
        Page<AuditEvent> auditPage;
        long totalEvents = 0;

        do {
            Pageable pageable = PageRequest.of(page++, size);
            auditPage = auditService.findEventsByDateRange(start, end, pageable);

            for (AuditEvent event : auditPage.getContent()) {
                report.append("-".repeat(80)).append("\n");
                report.append("Event ID: ").append(event.getEventId()).append("\n");
                report.append("Type: ").append(event.getEventType()).append("\n");
                report.append("User ID: ").append(event.getUserId()).append("\n");
                report.append("Service: ").append(event.getServiceSource()).append("\n");
                report.append("Action: ").append(event.getAction()).append("\n");
                report.append("IP: ").append(event.getIpAddress()).append("\n");
                report.append("Timestamp: ").append(event.getTimestamp().format(TIMESTAMP_FORMAT)).append("\n");
                report.append("Result: ").append(event.getResult()).append("\n");
                report.append("Risk Score: ").append(event.getRiskScore()).append("\n");
                if (event.getComplianceFlags() != null && !event.getComplianceFlags().isEmpty()) {
                    report.append("Compliance: ").append(String.join(", ", event.getComplianceFlags())).append("\n");
                }
                report.append("\n");
                totalEvents++;
            }
        } while (auditPage.hasNext());

        report.append("=".repeat(80)).append("\n");
        report.append("Total événements : ").append(totalEvents).append("\n");
        report.append("=".repeat(80)).append("\n");

        Files.writeString(Paths.get(filePath), report.toString());
        log.info("📄 [EXPORT] Report generated: {} ({} events)", filePath, totalEvents);
    }

    private String truncate(String value, int length) {
        if (value == null) return "";
        return value.length() > length ? value.substring(0, length - 3) + "..." : value;
    }

    private void publishExportCompletedEvent(String jobId, String filePath, String format) {
        try {
            // Si vous avez Kafka configuré :
            // kafkaTemplate.send("audit.export.completed", jobId,
            //     Map.of("jobId", jobId, "filePath", filePath, "format", format));
            log.info("📤 [EXPORT] Export completed event published for job: {}", jobId);
        } catch (Exception e) {
            log.warn("Failed to publish export completed event: {}", e.getMessage());
        }
    }

    private void publishExportFailedEvent(String jobId, String error) {
        try {

            kafkaTemplate.send("audit.export.failed", jobId,
               Map.of("jobId", jobId, "error", error));
            log.error("📤 [EXPORT] Export failed event published for job: {}", jobId);
        } catch (Exception e) {
            log.warn("Failed to publish export failed event: {}", e.getMessage());
        }
    }
}