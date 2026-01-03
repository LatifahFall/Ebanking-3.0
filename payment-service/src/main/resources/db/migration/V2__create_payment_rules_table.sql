-- Migration: Create payment_rules table
-- Version: 2
-- Description: Creates the payment_rules table for managing payment business rules

CREATE TABLE IF NOT EXISTS payment_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_type VARCHAR(50) NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    description TEXT,
    conditions JSONB NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT uq_rule_name UNIQUE (rule_name)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_payment_rules_enabled ON payment_rules(enabled);
CREATE INDEX IF NOT EXISTS idx_payment_rules_rule_type ON payment_rules(rule_type);
CREATE INDEX IF NOT EXISTS idx_payment_rules_priority ON payment_rules(priority DESC);
CREATE INDEX IF NOT EXISTS idx_payment_rules_enabled_priority ON payment_rules(enabled, priority DESC);

-- Index for JSONB conditions queries
CREATE INDEX IF NOT EXISTS idx_payment_rules_conditions ON payment_rules USING GIN (conditions);

COMMENT ON TABLE payment_rules IS 'Stores payment business rules and validation rules';
COMMENT ON COLUMN payment_rules.id IS 'Unique identifier for the rule';
COMMENT ON COLUMN payment_rules.rule_type IS 'Type of rule (e.g., AMOUNT_LIMIT, DAILY_LIMIT, FREQUENCY_LIMIT, etc.)';
COMMENT ON COLUMN payment_rules.rule_name IS 'Unique name for the rule';
COMMENT ON COLUMN payment_rules.description IS 'Description of what the rule does';
COMMENT ON COLUMN payment_rules.conditions IS 'Rule conditions in JSON format (e.g., {"maxAmount": 10000, "currency": "EUR"})';
COMMENT ON COLUMN payment_rules.enabled IS 'Whether the rule is currently active';
COMMENT ON COLUMN payment_rules.priority IS 'Rule priority (higher number = higher priority)';
COMMENT ON COLUMN payment_rules.created_at IS 'Timestamp when rule was created';
COMMENT ON COLUMN payment_rules.updated_at IS 'Timestamp when rule was last updated';
COMMENT ON COLUMN payment_rules.created_by IS 'User who created the rule';
COMMENT ON COLUMN payment_rules.updated_by IS 'User who last updated the rule';

