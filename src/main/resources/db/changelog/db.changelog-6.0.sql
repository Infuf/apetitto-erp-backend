--liquibase formatted sql

--changeset asilbek:44
CREATE INDEX idx_transfer_order_created_at ON transfer_order (created_at);

--changeset asilbek:45
ALTER TABLE transfer_order_item
    ADD COLUMN selling_price_snapshot NUMERIC(20, 2);

--changeset asilbek:46
UPDATE transfer_order_item toi
SET selling_price_snapshot = p.selling_price
FROM product p
WHERE toi.product_id = p.id;
CREATE INDEX idx_stock_movement_time ON stock_movement (movement_time);

--changeset asilbek:47
ALTER TABLE attendance_records
    ADD COLUMN total_less_minutes INT DEFAULT 0,
    ADD COLUMN early_come_minutes INT DEFAULT 0,
    ADD COLUMN late_out_minutes   INT DEFAULT 0;