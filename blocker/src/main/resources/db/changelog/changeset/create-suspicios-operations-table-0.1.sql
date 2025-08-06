CREATE SCHEMA IF NOT EXISTS bank;

CREATE TABLE bank.suspicious_operations
(
    id             BIGSERIAL PRIMARY KEY,
    login          VARCHAR(255)                NOT NULL,
    amount         NUMERIC(19, 2)              NOT NULL,
    operation_type VARCHAR(50)                 NOT NULL,
    timestamp      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    reasons        TEXT                        NOT NULL
);