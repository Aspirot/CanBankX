CREATE TABLE IF NOT EXISTS ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(19,4) NOT NULL,
    account_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    date TIMESTAMPTZ NOT NULL,
    entry_type VARCHAR(10) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ledger_account_date ON ledger_entries(account_id, date);
