-- ============================================================================
-- FICHIER: V3__create_audit_compliance_flags_table.sql
-- ============================================================================
CREATE TABLE IF NOT EXISTS audit.audit_compliance_flags (
                                                            event_id UUID NOT NULL,
                                                            flag VARCHAR(50) NOT NULL,
    PRIMARY KEY (event_id, flag),
    CONSTRAINT fk_audit_event
    FOREIGN KEY (event_id)
    REFERENCES audit.audit_events(id)
    ON DELETE CASCADE
    );

CREATE INDEX idx_compliance_flag ON audit.audit_compliance_flags(flag);
