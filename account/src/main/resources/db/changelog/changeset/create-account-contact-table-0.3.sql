CREATE SCHEMA IF NOT EXISTS bank;

CREATE TABLE bank.contacts
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    type        VARCHAR(50) NOT NULL,
    value       VARCHAR(255) NOT NULL UNIQUE,
    account_id  BIGINT NOT NULL,
    FOREIGN KEY (account_id) REFERENCES bank.accounts(id) ON DELETE CASCADE,
    UNIQUE (type, account_id)
);