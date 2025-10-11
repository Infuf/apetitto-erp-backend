--liquibase formatted sql

--changeset asilbek:9
ALTER TABLE stock_item
    ADD average_cost DECIMAL(20, 4) NOT NULL DEFAULT 0;

--changeset asilbek:10
ALTER TABLE stock_movement_item
    ADD cost_price DECIMAL(20, 4);

--changeset asilbek:11
ALTER TABLE product
    RENAME price TO selling_price;

--changeset asilbek:12
CREATE TABLE transfer_order
(
    id                       BIGSERIAL PRIMARY KEY,
    source_warehouse_id      BIGINT      NOT NULL REFERENCES warehouse (id),
    destination_warehouse_id BIGINT      NOT NULL REFERENCES warehouse (id),
    status                   VARCHAR(50) NOT NULL, -- PENDING, SHIPPED, RECEIVED, CANCELLED
    created_at               TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
    shipped_at               TIMESTAMP WITHOUT TIME ZONE,
    received_at              TIMESTAMP WITHOUT TIME ZONE
);

--changeset asilbek:13
CREATE TABLE transfer_order_item
(
    id                BIGSERIAL PRIMARY KEY,
    transfer_order_id BIGINT         NOT NULL REFERENCES transfer_order (id),
    product_id        BIGINT         NOT NULL REFERENCES product (id),
    quantity          BIGINT         NOT NULL,
    cost_at_transfer  DECIMAL(20, 4) NOT NULL
);
--changeset asilbek:14
ALTER TABLE category
    ADD CONSTRAINT uk_name UNIQUE (name);

--changeset asilbek:15
ALTER TABLE product
    ADD CONSTRAINT uk_product_code UNIQUE (product_code);

--changeset asilbek:16
ALTER TABLE product
    ADD CONSTRAINT uk_product_name UNIQUE (name);

--changeset asilbek:17
ALTER TABLE product
    ADD CONSTRAINT uk_product_barcode UNIQUE (barcode);