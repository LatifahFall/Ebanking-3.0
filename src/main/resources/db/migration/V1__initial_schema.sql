-- =============================================
-- V1__initial_schema.sql
-- Création du schéma initial pour account-service (version complète et cohérente)
-- =============================================

-- Table des comptes
CREATE TABLE accounts (
                          id BIGSERIAL PRIMARY KEY,
                          account_number VARCHAR(34) UNIQUE NOT NULL,
                          user_id BIGINT NOT NULL,
                          account_type VARCHAR(20) NOT NULL
                              CHECK (account_type IN ('CHECKING', 'SAVINGS', 'BUSINESS', 'INVESTMENT')),
                          currency VARCHAR(3) NOT NULL DEFAULT 'EUR'
                              CHECK (currency ~ '^[A-Z]{3}$'),
                          balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000
                              CHECK (balance >= 0),
                          final_balance DECIMAL(19,4)  CHECK (final_balance >= 0),

                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                              CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED', 'DORMANT')),

    -- Champs d'audit (obligatoires pour traçabilité bancaire)
                          suspension_reason VARCHAR(500),
                          suspended_by VARCHAR(100),
                          suspended_at TIMESTAMP,
                          closure_reason VARCHAR(500),
                          closed_by VARCHAR(100),
                          closed_at TIMESTAMP,

                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des transactions
CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              account_id BIGINT NOT NULL,
                              type VARCHAR(10) NOT NULL
                                  CHECK (type IN ('CREDIT', 'DEBIT')),
                              amount DECIMAL(19,4) NOT NULL
                                  CHECK (amount > 0),
                              balance_after DECIMAL(19,4) NOT NULL,
                              reference VARCHAR(100) UNIQUE,           -- Idempotence critique
                              description VARCHAR(500),
                              created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_account
                                  FOREIGN KEY (account_id)
                                      REFERENCES accounts(id)
                                      ON DELETE CASCADE
);

-- Indexes performants
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_transactions_reference ON transactions(reference);
CREATE INDEX idx_transactions_account_created ON transactions(account_id, created_at DESC);

-- Trigger pour mise à jour automatique de updated_at
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_accounts_updated_at
    BEFORE UPDATE ON accounts
    FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();

-- Données de test (uniquement pour dev/test — Flyway les applique, mais tu peux les désactiver via profile si besoin)
INSERT INTO accounts (
    account_number, user_id, account_type, currency, balance, final_balance, status
) VALUES
      ('MA6400012345678901234567890', 1001, 'CHECKING', 'EUR', 5000.00, 5000.00, 'ACTIVE'),
      ('MA6400098765432109876543210', 1001, 'SAVINGS', 'EUR', 15250.75, 15250.75, 'ACTIVE'),
      ('MA6400055556666777788889999', 2002, 'BUSINESS', 'USD', 30780.00, 30780.00, 'ACTIVE')
ON CONFLICT (account_number) DO NOTHING;
