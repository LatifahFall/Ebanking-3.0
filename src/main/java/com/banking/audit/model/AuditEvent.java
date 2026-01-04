package com.banking.audit.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "audit_events",
        schema = "audit",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_event_type", columnList = "event_type"),
                @Index(name = "idx_timestamp", columnList = "timestamp DESC"),
                @Index(name = "idx_service_source", columnList = "service_source"),
                @Index(name = "idx_risk_score", columnList = "risk_score"),
                @Index(name = "idx_composite_search", columnList = "user_id, event_type, timestamp")
        }
)
@Document(indexName = "audit_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @jakarta.persistence.Id
    @GeneratedValue
    @org.springframework.data.annotation.Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID eventId = UUID.randomUUID();

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime timestamp;

    @Column(name = "event_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    @Field(type = FieldType.Keyword)
    private EventType eventType;

    @Column(name = "user_id")
    @Field(type = FieldType.Long)
    private Long userId;

    @Column(name = "session_id", length = 255)
    @Field(type = FieldType.Keyword)
    private String sessionId;

    @Column(name = "service_source", nullable = false, length = 50)
    @Field(type = FieldType.Keyword)
    private String serviceSource;

    @Column(name = "action", nullable = false, length = 100)
    @Field(type = FieldType.Keyword)
    private String action;

    @Column(name = "resource_type", length = 50)
    @Field(type = FieldType.Keyword)
    private String resourceType;

    @Column(name = "resource_id", length = 255)
    @Field(type = FieldType.Keyword)
    private String resourceId;

    // ✅ CORRECTION: Ajout de @JdbcTypeCode pour JSONB
    @Column(name = "resource_details", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @Field(type = FieldType.Text, analyzer = "standard")
    private String resourceDetails;

    @Column(name = "ip_address", length = 45)
    @Field(type = FieldType.Ip)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    @Field(type = FieldType.Text)
    private String userAgent;

    @Column(name = "geolocation", length = 100)
    @Field(type = FieldType.Keyword)
    private String geolocation;

    @Column(name = "device_id", length = 255)
    @Field(type = FieldType.Keyword)
    private String deviceId;

    @Column(name = "result", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Field(type = FieldType.Keyword)
    private AuditResult result = AuditResult.SUCCESS;

    @Column(name = "error_details", columnDefinition = "TEXT")
    @Field(type = FieldType.Text)
    private String errorDetails;

    // ✅ CORRECTION: Ajout de @JdbcTypeCode pour JSONB
    @Column(name = "changes_before", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @Field(type = FieldType.Object)
    private String changesBefore;

    // ✅ CORRECTION: Ajout de @JdbcTypeCode pour JSONB
    @Column(name = "changes_after", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @Field(type = FieldType.Object)
    private String changesAfter;

    // ✅ CORRECTION: Ajout de @JdbcTypeCode pour JSONB
    @Column(name = "metadata", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @Field(type = FieldType.Object)
    private Map<String, String> metadata = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "audit_compliance_flags",
            joinColumns = @JoinColumn(name = "event_id")
    )
    @Column(name = "flag", nullable = false)
    @Field(type = FieldType.Keyword)
    private Set<String> complianceFlags;
    @Column(name = "risk_score")
    @Field(type = FieldType.Double)
    private Double riskScore = 0.0;

    @Column(name = "checksum", length = 64)
    @Field(type = FieldType.Keyword)
    private String checksum;

    @Column(name = "is_sensitive", nullable = false)
    private boolean sensitive = false;

    @Column(name = "retention_until")
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime retentionUntil;

    @PrePersist
    protected void onCreate() {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (result == null) {
            result = AuditResult.SUCCESS;
        }
        if (riskScore == null) {
            riskScore = 0.0;
        }
        if (metadata == null) {
            metadata = new HashMap<>();
        }
    }

    public enum EventType {
        USER_LOGIN, USER_LOGOUT, LOGIN_FAILED, MFA_REQUIRED, MFA_SUCCESS, MFA_FAILED,
        PASSWORD_RESET, NEW_DEVICE, ACCESS_DENIED, SUSPICIOUS_ACTIVITY,
        AUTHENTICATION,
        USER_CREATED, USER_UPDATED, USER_ACTIVATED, USER_DEACTIVATED,
        KYC_UPDATED, KYC_APPROVED, KYC_REJECTED,
        CONSENT_GRANTED, CONSENT_REVOKED,
        CLIENT_ASSIGNED, CLIENT_UNASSIGNED,
        ACCOUNT_CREATED, ACCOUNT_UPDATED, ACCOUNT_ACCESSED,
        ACCOUNT_BALANCE_CHANGED, ACCOUNT_SUSPENDED, ACCOUNT_CLOSED,
        PAYMENT_INITIATED, PAYMENT_CREATED, PAYMENT_COMPLETED,
        PAYMENT_FAILED, PAYMENT_REVERSED, TRANSACTION_COMPLETED,
        FRAUD_DETECTED,
        CRYPTO_PURCHASE, CRYPTO_SALE,
        NOTIFICATION_SENT, NOTIFICATION_FAILED,
        ADMIN_ACTION, CONFIG_CHANGED, SYSTEM_EVENT,
        LOGIN
    }

    public enum AuditResult {
        SUCCESS, FAILURE, PENDING, BLOCKED
    }
}