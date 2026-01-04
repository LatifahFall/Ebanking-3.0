-- Migration: Convert payments table from UUID to BIGINT
-- Version: 8
-- Description: Converts all UUID columns in payments table to BIGINT
--              This migration is safe to run even if columns are already BIGINT

-- Only run if table exists and columns are UUID type
DO $$
BEGIN
    -- Check if from_account_id is UUID and convert to BIGINT
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'payments' 
        AND column_name = 'from_account_id' 
        AND data_type = 'uuid'
    ) THEN
        -- Drop foreign key constraints first if they exist
        ALTER TABLE qr_code_payment DROP CONSTRAINT IF EXISTS fk_qr_code_payment_payment;
        
        -- Drop indexes on columns to be converted
        DROP INDEX IF EXISTS idx_payments_from_account;
        DROP INDEX IF EXISTS idx_payments_to_account;
        DROP INDEX IF EXISTS idx_payments_user_id;
        DROP INDEX IF EXISTS idx_payments_user_status;
        DROP INDEX IF EXISTS idx_payments_account_status;
        DROP INDEX IF EXISTS idx_qr_code_payment_payment_id;
        
        -- Drop primary key temporarily
        ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_pkey;
        
        -- Convert id column
        ALTER TABLE payments ALTER COLUMN id TYPE BIGINT USING (CAST(EXTRACT(EPOCH FROM created_at) * 1000 AS BIGINT) + CAST(RANDOM() * 1000 AS BIGINT));
        
        -- Convert other columns (using default values for conversion)
        ALTER TABLE payments ALTER COLUMN from_account_id TYPE BIGINT USING 1;
        ALTER TABLE payments ALTER COLUMN to_account_id TYPE BIGINT USING NULL;
        ALTER TABLE payments ALTER COLUMN user_id TYPE BIGINT USING NULL;
        
        -- Recreate primary key with sequence
        CREATE SEQUENCE IF NOT EXISTS payments_id_seq START 1;
        ALTER TABLE payments ALTER COLUMN id SET DEFAULT nextval('payments_id_seq');
        ALTER SEQUENCE payments_id_seq OWNED BY payments.id;
        ALTER TABLE payments ADD PRIMARY KEY (id);
        
        -- Recreate indexes
        CREATE INDEX idx_payments_from_account ON payments(from_account_id);
        CREATE INDEX idx_payments_to_account ON payments(to_account_id);
        CREATE INDEX idx_payments_user_id ON payments(user_id);
        CREATE INDEX idx_payments_user_status ON payments(user_id, status);
        CREATE INDEX idx_payments_account_status ON payments(from_account_id, status);
        
        RAISE NOTICE 'Converted payments table from UUID to BIGINT';
    ELSE
        RAISE NOTICE 'payments table columns are already BIGINT, skipping conversion';
    END IF;
    
    -- Convert qr_code_payment table if it exists and uses UUID
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_name = 'qr_code_payment'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'qr_code_payment' 
        AND column_name = 'payment_id' 
        AND data_type = 'uuid'
    ) THEN
        ALTER TABLE qr_code_payment DROP CONSTRAINT IF EXISTS fk_qr_code_payment_payment;
        DROP INDEX IF EXISTS idx_qr_code_payment_payment_id;
        DROP INDEX IF EXISTS idx_qr_code_payment_user_id;
        ALTER TABLE qr_code_payment DROP CONSTRAINT IF EXISTS qr_code_payment_pkey;
        ALTER TABLE qr_code_payment DROP CONSTRAINT IF EXISTS qr_code_payment_payment_id_key;
        
        ALTER TABLE qr_code_payment ALTER COLUMN id TYPE BIGINT USING (CAST(EXTRACT(EPOCH FROM created_at) * 1000 AS BIGINT) + CAST(RANDOM() * 1000 AS BIGINT));
        ALTER TABLE qr_code_payment ALTER COLUMN payment_id TYPE BIGINT USING 1;
        ALTER TABLE qr_code_payment ALTER COLUMN user_id TYPE BIGINT USING 1;
        ALTER TABLE qr_code_payment ALTER COLUMN from_account_id TYPE BIGINT USING 1;
        ALTER TABLE qr_code_payment ALTER COLUMN to_account_id TYPE BIGINT USING NULL;
        
        CREATE SEQUENCE IF NOT EXISTS qr_code_payment_id_seq START 1;
        ALTER TABLE qr_code_payment ALTER COLUMN id SET DEFAULT nextval('qr_code_payment_id_seq');
        ALTER SEQUENCE qr_code_payment_id_seq OWNED BY qr_code_payment.id;
        ALTER TABLE qr_code_payment ADD PRIMARY KEY (id);
        ALTER TABLE qr_code_payment ADD CONSTRAINT qr_code_payment_payment_id_key UNIQUE (payment_id);
        
        CREATE INDEX idx_qr_code_payment_payment_id ON qr_code_payment(payment_id);
        CREATE INDEX idx_qr_code_payment_user_id ON qr_code_payment(user_id);
        
        ALTER TABLE qr_code_payment ADD CONSTRAINT fk_qr_code_payment_payment 
            FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE;
        
        RAISE NOTICE 'Converted qr_code_payment table from UUID to BIGINT';
    ELSE
        RAISE NOTICE 'qr_code_payment table is already BIGINT or does not exist, skipping conversion';
    END IF;
END $$;

