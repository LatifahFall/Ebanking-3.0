-- Create qr_code_payment table for QR code based biometric payments
CREATE TABLE IF NOT EXISTS qr_code_payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    qr_token VARCHAR(255) NOT NULL UNIQUE,
    qr_code_data VARCHAR(1000) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    from_account_id UUID NOT NULL,
    to_account_id UUID,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_qr_code_payment_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_qr_code_payment_token ON qr_code_payment(qr_token);
CREATE INDEX idx_qr_code_payment_payment_id ON qr_code_payment(payment_id);
CREATE INDEX idx_qr_code_payment_user_id ON qr_code_payment(user_id);
CREATE INDEX idx_qr_code_payment_expires_at ON qr_code_payment(expires_at);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_qr_code_payment_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_qr_code_payment_updated_at
    BEFORE UPDATE ON qr_code_payment
    FOR EACH ROW
    EXECUTE FUNCTION update_qr_code_payment_updated_at();

