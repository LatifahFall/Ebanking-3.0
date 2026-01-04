-- ============================================================================
-- FICHIER: V10__create_audit_anomaly_detection_function.sql
-- ============================================================================
CREATE OR REPLACE FUNCTION audit.detect_user_anomalies(
    p_user_id BIGINT,
    p_hours INTEGER DEFAULT 24
)
RETURNS TABLE (
    anomaly_type VARCHAR,
    anomaly_count BIGINT,
    risk_level VARCHAR
) AS $$
BEGIN
RETURN QUERY
    WITH recent_activity AS (
        SELECT *
        FROM audit.audit_events
        WHERE user_id = p_user_id
          AND timestamp >= CURRENT_TIMESTAMP - (p_hours || ' hours')::INTERVAL
    ),
    anomalies AS (
        SELECT
            'MULTIPLE_FAILED_LOGINS' as anomaly_type,
            COUNT(*) as anomaly_count,
            CASE
                WHEN COUNT(*) >= 10 THEN 'CRITICAL'
                WHEN COUNT(*) >= 5 THEN 'HIGH'
                ELSE 'MEDIUM'
            END as risk_level
        FROM recent_activity
        WHERE event_type IN ('LOGIN_FAILED', 'ACCESS_DENIED')

        UNION ALL

        SELECT
            'MULTIPLE_IP_ADDRESSES' as anomaly_type,
            COUNT(DISTINCT ip_address) as anomaly_count,
            CASE
                WHEN COUNT(DISTINCT ip_address) >= 5 THEN 'HIGH'
                WHEN COUNT(DISTINCT ip_address) >= 3 THEN 'MEDIUM'
                ELSE 'LOW'
            END as risk_level
        FROM recent_activity
        WHERE ip_address IS NOT NULL

        UNION ALL

        SELECT
            'HIGH_RISK_EVENTS' as anomaly_type,
            COUNT(*) as anomaly_count,
            CASE
                WHEN COUNT(*) >= 5 THEN 'CRITICAL'
                WHEN COUNT(*) >= 3 THEN 'HIGH'
                ELSE 'MEDIUM'
            END as risk_level
        FROM recent_activity
        WHERE risk_score >= 0.7
    )
SELECT * FROM anomalies
WHERE anomaly_count > 0
ORDER BY
    CASE risk_level
        WHEN 'CRITICAL' THEN 1
        WHEN 'HIGH' THEN 2
        WHEN 'MEDIUM' THEN 3
        ELSE 4
        END;
END;
$$ LANGUAGE plpgsql;