-- Migration: Add BIOMETRIC payment type
-- Version: 4
-- Description: Adds BIOMETRIC as a new payment type option

-- Mettre à jour la contrainte CHECK pour inclure BIOMETRIC
ALTER TABLE payments 
DROP CONSTRAINT IF EXISTS payments_payment_type_check;

ALTER TABLE payments 
ADD CONSTRAINT payments_payment_type_check 
CHECK (payment_type IN ('STANDARD', 'INSTANT', 'RECURRING', 'BIOMETRIC'));

COMMENT ON COLUMN payments.payment_type IS 'Type of payment: STANDARD, INSTANT, RECURRING, or BIOMETRIC';

