-- ============================================================================
-- FICHIER: V6__create_audit_user_activity_view.sql
-- ============================================================================
CREATE OR REPLACE VIEW audit.audit_user_activity AS
SELECT
    user_id,
    DATE(timestamp) as activity_date,
    COUNT(*) as total_events,
    COUNT(DISTINCT event_type) as unique_event_types,
    COUNT(DISTINCT ip_address) as unique_ips,
    SUM(CASE WHEN result = 'FAILURE' THEN 1 ELSE 0 END) as failed_attempts,
    AVG(risk_score) as avg_risk_score,
    MAX(timestamp) as last_activity
FROM audit.audit_events
WHERE timestamp >= CURRENT_DATE - INTERVAL '90 days'
  AND user_id IS NOT NULL
GROUP BY user_id, DATE(timestamp)
ORDER BY user_id, activity_date DESC;