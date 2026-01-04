-- ============================================================================
-- FICHIER: V4__create_audit_export_jobs_table.sql
-- ============================================================================
CREATE TABLE IF NOT EXISTS audit.audit_export_jobs (
                                                       job_id VARCHAR(36) PRIMARY KEY,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    format VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    file_path TEXT,
    error_message TEXT,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    file_size_bytes BIGINT,
    events_count BIGINT
    );

CREATE INDEX idx_export_status ON audit.audit_export_jobs(status);
CREATE INDEX idx_export_created_at ON audit.audit_export_jobs(created_at DESC);
CREATE INDEX idx_export_created_by ON audit.audit_export_jobs(created_by);