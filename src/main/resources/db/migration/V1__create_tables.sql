-- Flyway migration V1: create initial tables for analytics service
-- Uses pgcrypto for gen_random_uuid(). If not available, install extension or adapt defaults.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- alerts
CREATE TABLE IF NOT EXISTS alerts (
    alert_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id text NOT NULL,
    alert_type varchar(100) NOT NULL,
    severity varchar(20) NOT NULL,
    title text NOT NULL,
    message text,
    threshold_value numeric(15,2),
    current_value numeric(15,2),
    status varchar(20) NOT NULL,
    triggered_at timestamp without time zone NOT NULL,
    resolved_at timestamp without time zone,
    notified boolean DEFAULT false
);
CREATE INDEX IF NOT EXISTS idx_user_status ON alerts (user_id, status);
CREATE INDEX IF NOT EXISTS idx_alert_type ON alerts (alert_type);

-- user_metrics
CREATE TABLE IF NOT EXISTS user_metrics (
    id bigserial PRIMARY KEY,
    user_id text NOT NULL,
    metric_date date NOT NULL,
    total_transactions integer,
    total_spent numeric(15,2),
    total_received numeric(15,2),
    account_balance numeric(15,2),
    crypto_value numeric(15,2),
    login_count integer,
    failed_transactions integer,
    notifications_sent integer,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);
CREATE INDEX IF NOT EXISTS idx_user_date ON user_metrics (user_id, metric_date);
CREATE INDEX IF NOT EXISTS idx_metric_date ON user_metrics (metric_date);

-- transaction_metrics
CREATE TABLE IF NOT EXISTS transaction_metrics (
    id bigserial PRIMARY KEY,
    metric_date date NOT NULL,
    transaction_type varchar(50),
    total_count bigint,
    total_amount numeric(15,2),
    avg_amount numeric(15,2),
    min_amount numeric(15,2),
    max_amount numeric(15,2),
    success_count bigint,
    failed_count bigint
);
CREATE INDEX IF NOT EXISTS idx_tx_date_type ON transaction_metrics (metric_date, transaction_type);

-- You can add further migrations for other entities/tables as needed.

