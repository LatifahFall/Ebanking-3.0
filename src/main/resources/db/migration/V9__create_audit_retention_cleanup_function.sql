-- ============================================================================
-- FICHIER: V9__create_audit_retention_cleanup_function.sql
-- ============================================================================
CREATE OR REPLACE FUNCTION audit.cleanup_expired_events()
RETURNS TABLE (
    deleted_count BIGINT,
    archived_count BIGINT
) AS $$
DECLARE
v_deleted_count BIGINT;
    v_archived_count BIGINT;
BEGIN
SELECT COUNT(*) INTO v_deleted_count
FROM audit.audit_events
WHERE retention_until < CURRENT_TIMESTAMP;

DELETE FROM audit.audit_events
WHERE retention_until < CURRENT_TIMESTAMP;

v_archived_count := v_deleted_count;

RETURN QUERY SELECT v_deleted_count, v_archived_count;
END;
$$ LANGUAGE plpgsql;
