-- ============================================================================
-- FICHIER: V2__create_audit_events_table.sql
-- ============================================================================
CREATE TABLE IF NOT EXISTS audit.audit_events (
                                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_type VARCHAR(100) NOT NULL,
    user_id BIGINT,
    session_id VARCHAR(255),
    service_source VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(255),
    resource_details JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    geolocation VARCHAR(100),
    device_id VARCHAR(255),
    result VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_details TEXT,
    changes_before JSONB,
    changes_after JSONB,
    risk_score NUMERIC(5,3) DEFAULT 0.0,
    checksum VARCHAR(64),
    is_sensitive BOOLEAN NOT NULL DEFAULT false,
    retention_until TIMESTAMP,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_user_id ON audit.audit_events(user_id);
CREATE INDEX idx_event_type ON audit.audit_events(event_type);
CREATE INDEX idx_timestamp ON audit.audit_events(timestamp DESC);
CREATE INDEX idx_service_source ON audit.audit_events(service_source);
CREATE INDEX idx_risk_score ON audit.audit_events(risk_score);
CREATE INDEX idx_composite_search ON audit.audit_events(user_id, event_type, timestamp);
CREATE INDEX idx_result ON audit.audit_events(result);
CREATE INDEX idx_session_id ON audit.audit_events(session_id);
CREATE INDEX idx_ip_address ON audit.audit_events(ip_address);
CREATE INDEX idx_retention_until ON audit.audit_events(retention_until);
CREATE INDEX idx_resource_details_gin ON audit.audit_events USING GIN(resource_details);
CREATE INDEX idx_metadata_gin ON audit.audit_events USING GIN(metadata);