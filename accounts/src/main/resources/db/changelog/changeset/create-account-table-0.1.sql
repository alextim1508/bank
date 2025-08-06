CREATE SCHEMA IF NOT EXISTS bank;

CREATE TABLE bank.accounts
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    login      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    birth_date DATE         NOT NULL,
    blocked    BOOLEAN   DEFAULT FALSE,
    roles      VARCHAR   DEFAULT '[]',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);