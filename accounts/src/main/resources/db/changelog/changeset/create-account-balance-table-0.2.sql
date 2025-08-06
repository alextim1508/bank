create table bank.balances
(
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    currency_code   VARCHAR(8)     NOT NULL,
    amount          NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    frozen_amount   NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    locked          BOOLEAN NOT NULL DEFAULT FALSE,
    locking_time    TIMESTAMP WITHOUT TIME ZONE NULL,
    opened          BOOLEAN NOT NULL DEFAULT TRUE,
    account_id      BIGINT REFERENCES bank.accounts (id) ON DELETE CASCADE,
    UNIQUE (currency_code, account_id)
);