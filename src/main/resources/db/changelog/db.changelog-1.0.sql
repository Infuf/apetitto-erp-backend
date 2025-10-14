--liquibase formatted sql

--changeset asilbek:1
CREATE TABLE category
(
    id          BIGSERIAL primary key,
    name        VARCHAR(255) NOT NULL,
    description TEXT
);

--changeset asilbek:2
CREATE TABLE product
(
    id           BIGSERIAL primary key,
    product_code VARCHAR(255),
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    unit         VARCHAR(255),
    barcode      VARCHAR(255),
    category_id  BIGINT       NOT NULL REFERENCES category (id),
    price        decimal(20, 2),
    created_at   TIMESTAMP DEFAULT now(),
    updated_at   TIMESTAMP DEFAULT now()
);

--changeset asilbek:3
CREATE TABLE warehouse
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255),
    location    TEXT,
    description TEXT
);

--changeset asilbek:4
CREATE TABLE stock_item
(
    id           BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT REFERENCES warehouse (id),
    product_id   BIGINT REFERENCES product (id),
    quantity     BIGINT,
    created_at   TIMESTAMP DEFAULT now(),
    updated_at   TIMESTAMP DEFAULT now()
);
--changeset asilbek:5
CREATE TABLE stock_movement
(
    id            BIGSERIAL PRIMARY KEY,
    warehouse_id  BIGINT REFERENCES warehouse (id),
    movement_type VARCHAR(255),
    movement_time timestamp DEFAULT now(),
    created_by    BIGINT,
    comment       TEXT
);

--changeset asilbek:6
CREATE TABLE stock_movement_item
(
    id          BIGSERIAL PRIMARY KEY,
    movement_id BIGINT REFERENCES stock_movement (id) NOT NULL,
    product_id  BIGINT REFERENCES product (id)        NOT NULL,
    quantity    BIGINT                                NOT NULL
);