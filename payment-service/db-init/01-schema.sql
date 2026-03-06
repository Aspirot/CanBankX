CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    dtype VARCHAR(31) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    creation_date TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    source_account_id BIGINT,
    destination_account_id BIGINT,
    destination_client_id BIGINT,
    destination_email VARCHAR(150)
);

CREATE INDEX IF NOT EXISTS idx_transactions_source_account_id ON transactions(source_account_id);
