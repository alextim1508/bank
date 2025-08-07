CREATE SCHEMA IF NOT EXISTS bank;

CREATE TABLE bank.notification_outbox
(
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,                    -- тип сущности: "Account", "Transfer", "Cash"
    aggregate_login VARCHAR(50) NOT NULL,                   -- login сущности: "ivan"
    event_type VARCHAR(100) NOT NULL,                       -- тип события: "ACCOUNT_CREATED", "TRANSFER_COMPLETED"
    payload VARCHAR(100) NOT NULL,                          -- полезная нагрузка
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',          -- статус: PENDING, PROCESSING, SENT, FAILED
    retry_count INT NOT NULL DEFAULT 0,                     -- Номер попытки отправки
    max_retries INT NOT NULL DEFAULT 3,                     -- Макс. количество попыток
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),      -- Когда сообщение было создано
    processed_at TIMESTAMP WITH TIME ZONE,                  -- Когда оно было успешно отправлено
    next_retry_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- Когда нужно попробовать отправить снова
    last_error TEXT
);

CREATE INDEX idx_notification_outbox_status_next_retry ON bank.notification_outbox (status, next_retry_at);

CREATE INDEX idx_notification_outbox_aggregate_id ON bank.notification_outbox (aggregate_login);

CREATE INDEX idx_notification_outbox_event_type ON bank.notification_outbox (event_type);