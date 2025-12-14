--liquibase formatted sql

--changeset asilbek:31
CREATE TABLE finance_category
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE finance_subcategory
(
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT       NOT NULL REFERENCES finance_category (id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_fin_cat_type ON finance_category (type);



CREATE TABLE finance_account
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(150)   NOT NULL,
    type       VARCHAR(50)    NOT NULL,
    balance    NUMERIC(20, 2) NOT NULL DEFAULT 0.00,
    user_id    BIGINT REFERENCES users (id),
    is_active  BOOLEAN                 DEFAULT TRUE,
    created_at TIMESTAMP               DEFAULT NOW(),
    updated_at TIMESTAMP               DEFAULT NOW()
);

CREATE INDEX idx_fin_acc_type ON finance_account (type);



CREATE TABLE finance_transaction
(
    id                  BIGSERIAL PRIMARY KEY,
    transaction_date    TIMESTAMP      NOT NULL,
    from_account_id     BIGINT REFERENCES finance_account (id),
    to_account_id       BIGINT REFERENCES finance_account (id),
    amount              NUMERIC(20, 2) NOT NULL CHECK (amount > 0),
    operation_type      VARCHAR(50)    NOT NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'COMPLETED',
    category_id         BIGINT REFERENCES finance_category (id),
    subcategory_id      BIGINT REFERENCES finance_subcategory (id),
    description         TEXT,
    created_by          BIGINT REFERENCES users (id),
    cancelled_by        BIGINT REFERENCES users (id),
    cancellation_reason TEXT,
    source_entity       VARCHAR(50),
    source_id           BIGINT,
    created_at          TIMESTAMP               DEFAULT NOW(),
    updated_at          TIMESTAMP               DEFAULT NOW()
);

ALTER TABLE finance_transaction
    ADD CONSTRAINT chk_ft_direction
        CHECK (from_account_id IS NOT NULL OR to_account_id IS NOT NULL);

ALTER TABLE finance_transaction
    ADD CONSTRAINT chk_ft_diff_accounts
        CHECK (from_account_id <> to_account_id);

CREATE INDEX idx_ft_date ON finance_transaction (transaction_date);
CREATE INDEX idx_ft_from ON finance_transaction (from_account_id);
CREATE INDEX idx_ft_to ON finance_transaction (to_account_id);
CREATE INDEX idx_ft_type ON finance_transaction (operation_type);



CREATE TABLE finance_transaction_item
(
    id             BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT         NOT NULL REFERENCES finance_transaction (id) ON DELETE CASCADE,

    product_id     BIGINT         NOT NULL REFERENCES product (id),

    quantity       NUMERIC(20, 4) NOT NULL,
    price_snapshot NUMERIC(20, 2) NOT NULL,
    total_amount   NUMERIC(20, 2) NOT NULL,

    created_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_fti_transaction ON finance_transaction_item (transaction_id);
CREATE INDEX idx_fti_product ON finance_transaction_item (product_id);

--changeset asilbek:32
ALTER TABLE finance_category
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE finance_subcategory
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
--changeset asilbek:33
ALTER TABLE finance_account
    ADD COLUMN description TEXT;

--changeset asilbek:34
ALTER TABLE finance_account
    ADD COLUMN discount_percentage NUMERIC(5, 2) DEFAULT 0.00;
