--liquibase formatted sql

--changeset asilbek:23
ALTER TABLE stock_movement
    ALTER COLUMN movement_time TYPE TIMESTAMPTZ USING movement_time AT TIME ZONE 'UTC';
ALTER TABLE stock_movement
    ALTER COLUMN movement_time SET DEFAULT (now() AT TIME ZONE 'utc');

--changeset asilbek:24
ALTER TABLE product
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';
ALTER TABLE product
    ALTER COLUMN created_at SET DEFAULT (now() AT TIME ZONE 'utc');

ALTER TABLE product
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';
ALTER TABLE product
    ALTER COLUMN updated_at SET DEFAULT (now() AT TIME ZONE 'utc');

ALTER TABLE stock_item
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';
ALTER TABLE stock_item
    ALTER COLUMN created_at SET DEFAULT (now() AT TIME ZONE 'utc');

ALTER TABLE stock_item
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';
ALTER TABLE stock_item
    ALTER COLUMN updated_at SET DEFAULT (now() AT TIME ZONE 'utc');

ALTER TABLE transfer_order
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';
ALTER TABLE transfer_order
    ALTER COLUMN created_at SET DEFAULT (now() AT TIME ZONE 'utc');

ALTER TABLE transfer_order
    ALTER COLUMN shipped_at TYPE TIMESTAMPTZ USING shipped_at AT TIME ZONE 'UTC';
ALTER TABLE transfer_order
    ALTER COLUMN received_at TYPE TIMESTAMPTZ USING received_at AT TIME ZONE 'UTC';
