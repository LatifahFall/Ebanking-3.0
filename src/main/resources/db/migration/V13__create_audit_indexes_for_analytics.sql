-- ============================================================================
-- V13__create_audit_indexes_for_analytics.sql
-- ============================================================================

-- Index pour analytics et rapports
CREATE INDEX IF NOT EXISTS idx_timestamp_user_event
    ON audit.audit_events(timestamp DESC, user_id, event_type);

CREATE INDEX IF NOT EXISTS idx_service_timestamp
    ON audit.audit_events(service_source, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_compliance_analytics
    ON audit.audit_events(timestamp DESC, result, risk_score)
    WHERE is_sensitive = true;

-- ❌ SUPPRIMÉ: Index partiel avec CURRENT_DATE (ne fonctionne pas avec PostgreSQL)
-- PostgreSQL n'accepte pas les fonctions non-IMMUTABLE dans les prédicats d'index
-- CREATE INDEX IF NOT EXISTS idx_recent_events
--     ON audit.audit_events(timestamp DESC, event_type)
--     WHERE timestamp >= CURRENT_DATE - INTERVAL '30 days';

-- ✅ SOLUTION: Index simple sur timestamp (performant pour les requêtes récentes)
CREATE INDEX IF NOT EXISTS idx_recent_events
    ON audit.audit_events(timestamp DESC, event_type);