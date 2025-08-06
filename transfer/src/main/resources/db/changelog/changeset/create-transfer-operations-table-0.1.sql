CREATE SCHEMA IF NOT EXISTS bank;

CREATE TABLE bank.transfer_operations
(
    id                        BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    from_login                VARCHAR(255)                              NOT NULL,
    from_currency             VARCHAR(10)                               NOT NULL,
    to_login                  VARCHAR(255)                              NOT NULL,
    to_currency               VARCHAR(10)                               NOT NULL,
    amount                    NUMERIC(19, 2)                            NOT NULL,
    converted_amount          NUMERIC(19, 2)                            NOT NULL,
    from_exchange_rate_to_rub DOUBLE PRECISION,
    operation_type            VARCHAR(50)                               NOT NULL,
    created_at                TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL
);