-- Script SQL pour convertir les tables de UUID à BIGINT
-- À exécuter manuellement dans votre base de données PostgreSQL

-- 1. Supprimer les contraintes de clé étrangère
ALTER TABLE qr_code_payment DROP CONSTRAINT IF EXISTS fk_qr_code_payment_payment;

-- 2. Supprimer les index
DROP INDEX IF EXISTS idx_payments_from_account;
DROP INDEX IF EXISTS idx_payments_to_account;
DROP INDEX IF EXISTS idx_payments_user_id;
DROP INDEX IF EXISTS idx_payments_user_status;
DROP INDEX IF EXISTS idx_payments_account_status;
DROP INDEX IF EXISTS idx_qr_code_payment_payment_id;
DROP INDEX IF EXISTS idx_qr_code_payment_user_id;

-- 3. Supprimer les contraintes de clé primaire temporairement
ALTER TABLE qr_code_payment DROP CONSTRAINT IF EXISTS qr_code_payment_pkey;
ALTER TABLE qr_code_payment DROP CONSTRAINT IF EXISTS qr_code_payment_payment_id_key;
ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_pkey;

-- 4. Convertir la table payments
ALTER TABLE payments ALTER COLUMN id TYPE BIGINT USING (CAST(EXTRACT(EPOCH FROM created_at) * 1000 AS BIGINT) + CAST(RANDOM() * 1000 AS BIGINT));
ALTER TABLE payments ALTER COLUMN from_account_id TYPE BIGINT USING 1;
ALTER TABLE payments ALTER COLUMN to_account_id TYPE BIGINT USING NULL;
ALTER TABLE payments ALTER COLUMN user_id TYPE BIGINT USING NULL;

-- 5. Créer la séquence pour payments.id
CREATE SEQUENCE IF NOT EXISTS payments_id_seq START 1;
ALTER TABLE payments ALTER COLUMN id SET DEFAULT nextval('payments_id_seq');
ALTER SEQUENCE payments_id_seq OWNED BY payments.id;
ALTER TABLE payments ADD PRIMARY KEY (id);

-- 6. Recréer les index pour payments
CREATE INDEX idx_payments_from_account ON payments(from_account_id);
CREATE INDEX idx_payments_to_account ON payments(to_account_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_user_status ON payments(user_id, status);
CREATE INDEX idx_payments_account_status ON payments(from_account_id, status);

-- 7. Convertir la table qr_code_payment (si elle existe)
ALTER TABLE qr_code_payment ALTER COLUMN id TYPE BIGINT USING (CAST(EXTRACT(EPOCH FROM created_at) * 1000 AS BIGINT) + CAST(RANDOM() * 1000 AS BIGINT));
ALTER TABLE qr_code_payment ALTER COLUMN payment_id TYPE BIGINT USING 1;
ALTER TABLE qr_code_payment ALTER COLUMN user_id TYPE BIGINT USING 1;
ALTER TABLE qr_code_payment ALTER COLUMN from_account_id TYPE BIGINT USING 1;
ALTER TABLE qr_code_payment ALTER COLUMN to_account_id TYPE BIGINT USING NULL;

-- 8. Créer la séquence pour qr_code_payment.id
CREATE SEQUENCE IF NOT EXISTS qr_code_payment_id_seq START 1;
ALTER TABLE qr_code_payment ALTER COLUMN id SET DEFAULT nextval('qr_code_payment_id_seq');
ALTER SEQUENCE qr_code_payment_id_seq OWNED BY qr_code_payment.id;
ALTER TABLE qr_code_payment ADD PRIMARY KEY (id);
ALTER TABLE qr_code_payment ADD CONSTRAINT qr_code_payment_payment_id_key UNIQUE (payment_id);

-- 9. Recréer les index pour qr_code_payment
CREATE INDEX idx_qr_code_payment_payment_id ON qr_code_payment(payment_id);
CREATE INDEX idx_qr_code_payment_user_id ON qr_code_payment(user_id);

-- 10. Recréer la contrainte de clé étrangère
ALTER TABLE qr_code_payment ADD CONSTRAINT fk_qr_code_payment_payment 
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE;

-- Alternative plus simple : Supprimer les tables et laisser Hibernate les recréer
-- ATTENTION : Cela supprimera toutes les données !
-- DROP TABLE IF EXISTS qr_code_payment CASCADE;
-- DROP TABLE IF EXISTS payments CASCADE;

