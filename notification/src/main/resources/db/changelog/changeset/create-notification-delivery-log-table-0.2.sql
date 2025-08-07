CREATE TABLE bank.notification_delivery_log
(
    id            BIGSERIAL PRIMARY KEY,
    outbox_id     BIGINT       NOT NULL REFERENCES bank.notification_outbox (id),
    channel       VARCHAR(20)  NOT NULL,                -- "EMAIL", "TELEGRAM", "SMS"
    recipient     VARCHAR(100) NOT NULL,                -- "ivan_ivanov"
    sent_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    status        VARCHAR(20)  NOT NULL,                -- "SUCCESS", "FAILED"
    error_message TEXT
);

CREATE INDEX idx_delivery_log_outbox_id ON bank.notification_delivery_log (outbox_id);