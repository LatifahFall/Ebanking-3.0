-- ============================================================================
-- FICHIER: V7__create_audit_high_risk_events_view.sql
-- ============================================================================
CREATE OR REPLACE VIEW audit.audit_high_risk_events AS
SELECT
    id,
    timestamp,
    event_type,
    user_id,
    service_source,
    action,
    ip_address,
    result,
    risk_score,
    error_details
FROM audit.audit_events
WHERE risk_score >= 0.7
  AND timestamp >= CURRENT_DATE - INTERVAL '7 days'
ORDER BY risk_score DESC, timestamp DESC;