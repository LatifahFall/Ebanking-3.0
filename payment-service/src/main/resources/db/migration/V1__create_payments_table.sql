-- Migration: Create payments table
-- Version: 1
-- Description: Creates the payments table with all necessary fields and indexes
-- Using BIGINT for IDs to align with account-service and user-service

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT,
    amount DECIMAL(19, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    payment_type VARCHAR(20) NOT NULL CHECK (payment_type IN ('STANDARD', 'INSTANT', 'RECURRING', 'QR_CODE', 'BIOMETRIC')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REVERSED')) DEFAULT 'PENDING',
    beneficiary_name VARCHAR(255),
    reference VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    reversed_at TIMESTAMP,
    reversal_reason VARCHAR(255),
    user_id BIGINT,
    description TEXT
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_payments_from_account ON payments(from_account_id);
CREATE INDEX IF NOT EXISTS idx_payments_to_account ON payments(to_account_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_payment_type ON payments(payment_type);

-- Composite index for common queries
CREATE INDEX IF NOT EXISTS idx_payments_user_status ON payments(user_id, status);
CREATE INDEX IF NOT EXISTS idx_payments_account_status ON payments(from_account_id, status);

-- Index for JSONB metadata queries (if needed)
CREATE INDEX IF NOT EXISTS idx_payments_metadata ON payments USING GIN (metadata);

COMMENT ON TABLE payments IS 'Stores all payment and transfer transactions';
COMMENT ON COLUMN payments.id IS 'Unique identifier for the payment (BIGINT)';
COMMENT ON COLUMN payments.from_account_id IS 'Source account ID (BIGINT from account-service)';
COMMENT ON COLUMN payments.to_account_id IS 'Destination account ID (BIGINT from account-service, nullable for withdrawals)';
COMMENT ON COLUMN payments.amount IS 'Payment amount (must be positive)';
COMMENT ON COLUMN payments.currency IS 'Currency code (ISO 4217)';
COMMENT ON COLUMN payments.payment_type IS 'Type of payment: STANDARD, INSTANT, RECURRING, QR_CODE, or BIOMETRIC';
COMMENT ON COLUMN payments.status IS 'Current status of the payment';
COMMENT ON COLUMN payments.beneficiary_name IS 'Name of the beneficiary';
COMMENT ON COLUMN payments.reference IS 'Payment reference number';
COMMENT ON COLUMN payments.metadata IS 'Additional metadata in JSON format';
COMMENT ON COLUMN payments.created_at IS 'Timestamp when payment was created';
COMMENT ON COLUMN payments.updated_at IS 'Timestamp when payment was last updated';
COMMENT ON COLUMN payments.completed_at IS 'Timestamp when payment was completed';
COMMENT ON COLUMN payments.reversed_at IS 'Timestamp when payment was reversed';
COMMENT ON COLUMN payments.reversal_reason IS 'Reason for payment reversal';
COMMENT ON COLUMN payments.user_id IS 'ID of the user who initiated the payment (BIGINT from user-service)';
COMMENT ON COLUMN payments.description IS 'Payment description';
