CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    from_account VARCHAR(10),
    to_account VARCHAR(10),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL,
    type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE INDEX idx_tx_by_from_account ON transactions(from_account);
CREATE INDEX idx_tx_by_to_account ON transactions(to_account);
CREATE INDEX idx_tx_by_type ON transactions(type);
CREATE INDEX idx_tx_by_timestamp ON transactions(timestamp);
