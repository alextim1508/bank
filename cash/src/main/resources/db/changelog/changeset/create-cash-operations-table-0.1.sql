CREATE SCHEMA IF NOT EXISTS bank;

CREATE TABLE bank.cash_operations
(
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    login          VARCHAR(255)                              NOT NULL,
    currency       VARCHAR(10)                               NOT NULL,
    amount         NUMERIC(19, 2)                            NOT NULL,
    operation_type VARCHAR(50)                               NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL
);