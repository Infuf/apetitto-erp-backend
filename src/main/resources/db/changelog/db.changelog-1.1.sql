--liquibase formatted sql

--changeset asilbek:7
ALTER TABLE public.stock_item
    ADD CONSTRAINT uq_stock_item_warehouse_product UNIQUE (warehouse_id, product_id);

--changeset asilbek:8
ALTER TABLE stock_movement
    ADD CONSTRAINT check_movement_type CHECK (movement_type IN
                                              ('INBOUND', 'OUTBOUND', 'TRANSFER_OUT', 'TRANSFER_IN', 'ADJUSTMENT'));