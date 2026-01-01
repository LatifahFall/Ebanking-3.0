-- Test schema for H2 database
-- Creates tables compatible with H2 (using VARCHAR instead of JSONB)

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    from_account_id UUID NOT NULL,
    to_account_id UUID,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    beneficiary_name VARCHAR(255),
    reference VARCHAR(255),
    metadata VARCHAR(10000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    reversed_at TIMESTAMP,
    reversal_reason VARCHAR(255),
    user_id UUID,
    description TEXT
);

CREATE INDEX IF NOT EXISTS idx_payments_from_account ON payments(from_account_id);
CREATE INDEX IF NOT EXISTS idx_payments_to_account ON payments(to_account_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_payment_type ON payments(payment_type);

CREATE TABLE IF NOT EXISTS payment_rules (
    id UUID PRIMARY KEY,
    rule_type VARCHAR(50) NOT NULL,
    rule_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    conditions VARCHAR(10000) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_payment_rules_enabled ON payment_rules(enabled);
CREATE INDEX IF NOT EXISTS idx_payment_rules_rule_type ON payment_rules(rule_type);
CREATE INDEX IF NOT EXISTS idx_payment_rules_priority ON payment_rules(priority);

