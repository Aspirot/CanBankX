CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    account_number VARCHAR(32) NOT NULL UNIQUE,
    balance NUMERIC(19,4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_accounts_client_id ON accounts(client_id);
