--liquibase formatted sql

--changeset asilbek:43
CREATE TABLE hr_device_log
(
    id            BIGSERIAL PRIMARY KEY,
    device_sn     VARCHAR(50) NOT NULL,
    user_pin      BIGINT      NOT NULL,
    event_time    TIMESTAMP   NOT NULL,
    event_type    INT,

    raw_data      TEXT,
    is_processed  BOOLEAN     NOT NULL DEFAULT FALSE,
    error_message TEXT,

    created_at    TIMESTAMPTZ          DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE INDEX idx_hr_device_log_processed ON hr_device_log (is_processed) WHERE is_processed = FALSE;