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
--changeset asilbek:25
ALTER TABLE roles
    ALTER COLUMN name TYPE VARCHAR(100);

--changeset asilbek:26
INSERT INTO roles (id, name)
VALUES (4, 'ROLE_WAREHOUSE_MANAGER'),
       (5, 'ROLE_STORE_MANAGER'),
       (6, 'ROLE_DEPARTMENT_MANAGER'),
       (7, 'ROLE_FINANCE_OFFICER'),
       (8, 'ROLE_OWNER')
ON CONFLICT (id) DO NOTHING;
SELECT setval('roles_id_seq', (SELECT COALESCE(MAX(id), 0) FROM roles), true);

--changeset asilbek:27
ALTER TABLE users
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT false;

--changeset asilbek:28
ALTER TABLE users
    ADD COLUMN warehouse_id BIGINT NULL;
ALTER TABLE users
    ADD CONSTRAINT fk_users_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id);

--changeset asilbek:29
ALTER TABLE stock_movement
    DROP CONSTRAINT check_movement_type;

--changeset asilbek:30
ALTER TABLE stock_movement
    ADD CONSTRAINT check_movement_type CHECK (movement_type IN
                                              ('INBOUND', 'OUTBOUND', 'TRANSFER_OUT', 'TRANSFER_IN', 'ADJUSTMENT',
                                               'SELL'));