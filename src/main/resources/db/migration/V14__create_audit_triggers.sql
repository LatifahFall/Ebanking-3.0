-- ============================================================================
-- FICHIER: V14__create_audit_triggers.sql
-- ============================================================================
CREATE OR REPLACE FUNCTION audit.update_checksum()
RETURNS TRIGGER AS $$
BEGIN
RETURN NEW;
END;
$$ LANGUAGE plpgsql;