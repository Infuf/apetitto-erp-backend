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

--changeset asilbek:48
DROP TABLE IF EXISTS payroll_accruals;
CREATE TABLE payroll_accruals
(
    id                               BIGSERIAL PRIMARY KEY,
    employee_id                      BIGINT         NOT NULL REFERENCES employees (id),

    period_start                     DATE           NOT NULL,
    period_end                       DATE           NOT NULL,

    payment_type                     VARCHAR(50)    NOT NULL,

    base_salary                      DECIMAL(20, 2) NOT NULL,
    base_work_hours                  DECIMAL(4, 2)  NOT NULL,
    base_days_off                    INT            DEFAULT 2,

    calculated_day_rate              DECIMAL(20, 2) NOT NULL,
    calculated_hour_rate             DECIMAL(20, 2) NOT NULL,

    days_worked                      INT            DEFAULT 0,
    total_worked_hours               DECIMAL(10, 2) DEFAULT 0,

    late_minutes                     INT            DEFAULT 0,
    early_leave_minutes              INT            DEFAULT 0,
    total_undertime_minutes          INT            DEFAULT 0,

    early_come_minutes               INT            DEFAULT 0,
    late_out_minutes                 INT            DEFAULT 0,
    total_overtime_minutes           INT            DEFAULT 0,
    total_overtime_effective_minutes INT            DEFAULT 0,

    gross_salary_amount              DECIMAL(20, 2) NOT NULL,
    penalty_amount                   DECIMAL(20, 2) DEFAULT 0,
    overtime_bonus_amount            DECIMAL(20, 2) DEFAULT 0,
    manual_bonus_amount              DECIMAL(20, 2) DEFAULT 0,
    final_amount                     DECIMAL(20, 2) NOT NULL,

    finance_transaction_id           BIGINT REFERENCES finance_transaction (id),
    status                           VARCHAR(50),
    calculation_note                 TEXT,

    created_by                       BIGINT REFERENCES users (id),
    created_at                       TIMESTAMPTZ    DEFAULT (now() AT TIME ZONE 'utc')
);
