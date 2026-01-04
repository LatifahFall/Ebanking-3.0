-- ============================================================================
-- FICHIER: V8__create_audit_compliance_report_function.sql
-- ============================================================================
CREATE OR REPLACE FUNCTION audit.generate_compliance_report(
    p_start_date TIMESTAMP,
    p_end_date TIMESTAMP,
    p_compliance_flag VARCHAR DEFAULT NULL
)
RETURNS TABLE (
    event_type VARCHAR,
    total_events BIGINT,
    success_count BIGINT,
    failure_count BIGINT,
    unique_users BIGINT,
    avg_risk_score NUMERIC
) AS $$
BEGIN
RETURN QUERY
SELECT
    ae.event_type,
    COUNT(*) as total_events,
    SUM(CASE WHEN ae.result = 'SUCCESS' THEN 1 ELSE 0 END) as success_count,
    SUM(CASE WHEN ae.result = 'FAILURE' THEN 1 ELSE 0 END) as failure_count,
    COUNT(DISTINCT ae.user_id) as unique_users,
    AVG(ae.risk_score) as avg_risk_score
FROM audit.audit_events ae
         LEFT JOIN audit.audit_compliance_flags acf ON ae.id = acf.event_id
WHERE ae.timestamp BETWEEN p_start_date AND p_end_date
  AND (p_compliance_flag IS NULL OR acf.flag = p_compliance_flag)
GROUP BY ae.event_type
ORDER BY total_events DESC;
END;
$$ LANGUAGE plpgsql;