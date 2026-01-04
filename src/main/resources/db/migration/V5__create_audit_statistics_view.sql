-- ============================================================================
-- FICHIER: V5__create_audit_statistics_view.sql
-- ============================================================================
CREATE OR REPLACE VIEW audit.audit_statistics AS
SELECT
    DATE(timestamp) as audit_date,
    event_type,
    service_source,
    result,
    COUNT(*) as event_count,
    COUNT(DISTINCT user_id) as unique_users,
    AVG(risk_score) as avg_risk_score,
    MAX(risk_score) as max_risk_score,
    SUM(CASE WHEN risk_score >= 0.7 THEN 1 ELSE 0 END) as high_risk_count
FROM audit.audit_events
WHERE timestamp >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(timestamp), event_type, service_source, result
ORDER BY audit_date DESC, event_count DESC;
