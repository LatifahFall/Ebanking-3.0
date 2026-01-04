-- ============================================================================
-- FICHIER: V15__grant_permissions.sql
-- ============================================================================
-- ATTENTION: Changer les mots de passe en production !

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'audit_service') THEN
CREATE ROLE audit_service WITH LOGIN PASSWORD 'change_me_in_production';
END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'audit_reader') THEN
CREATE ROLE audit_reader WITH LOGIN PASSWORD 'change_me_in_production';
END IF;
END
$$;

GRANT USAGE ON SCHEMA audit TO audit_service;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA audit TO audit_service;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA audit TO audit_service;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA audit TO audit_service;

GRANT USAGE ON SCHEMA audit TO audit_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA audit TO audit_reader;
GRANT EXECUTE ON FUNCTION audit.generate_compliance_report TO audit_reader;
GRANT EXECUTE ON FUNCTION audit.detect_user_anomalies TO audit_reader;