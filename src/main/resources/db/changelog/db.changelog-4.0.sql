--liquibase formatted sql

--changeset asilbek:36
CREATE TABLE departments
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    manager_id  BIGINT,
    description TEXT
);

ALTER TABLE departments
    ADD CONSTRAINT fk_dept_manager FOREIGN KEY (manager_id) REFERENCES users (id);

--changeset asilbek:37
CREATE TABLE employees
(
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT         NOT NULL UNIQUE REFERENCES users (id),
    department_id      BIGINT REFERENCES departments (id),
    finance_account_id BIGINT         NOT NULL REFERENCES finance_account (id),

    position_title     VARCHAR(100),
    is_active          BOOLEAN                 DEFAULT TRUE,
    salary_base        DECIMAL(20, 2) NOT NULL DEFAULT 0,
    salary_type        VARCHAR(50)    NOT NULL CHECK (salary_type IN ('HOURLY', 'DAILY_SHIFT','FIXED')),

    days_off_per_month INT                     DEFAULT 2,
    calculation_days   INT                     DEFAULT 30,

    work_hours_per_day DECIMAL(4, 2)           DEFAULT 9.00,
    shift_start_time   TIME WITHOUT TIME ZONE,
    shift_end_time     TIME WITHOUT TIME ZONE,

    hired_at           TIMESTAMPTZ             DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at         TIMESTAMPTZ             DEFAULT (now() AT TIME ZONE 'utc')
);

--changeset asilbek:38
CREATE TABLE attendance_records
(
    id               BIGSERIAL PRIMARY KEY,
    employee_id      BIGINT      NOT NULL REFERENCES employees (id),
    date             DATE        NOT NULL,
    check_in         TIMESTAMPTZ,
    check_out        TIMESTAMPTZ,
    status           VARCHAR(50) NOT NULL DEFAULT 'ABSENT',
    duration_minutes INT                  DEFAULT 0,
    created_at       TIMESTAMPTZ          DEFAULT (now() AT TIME ZONE 'utc'),
    UNIQUE (employee_id, date)
);

--changeset asilbek:39
CREATE TABLE payroll_accruals
(
    id                     BIGSERIAL PRIMARY KEY,
    employee_id            BIGINT         NOT NULL REFERENCES employees (id),

    period_start           DATE           NOT NULL,
    period_end             DATE           NOT NULL,

    days_worked            INT            DEFAULT 0,
    norm_hours             DECIMAL(10, 2) NOT NULL,
    actual_hours           DECIMAL(10, 2) NOT NULL,
    overtime_hours         DECIMAL(10, 2) DEFAULT 0,
    undertime_hours        DECIMAL(10, 2) DEFAULT 0,



    base_amount            DECIMAL(20, 2) NOT NULL,
    overtime_amount        DECIMAL(20, 2) DEFAULT 0,
    bonus_amount           DECIMAL(20, 2) DEFAULT 0,
    final_amount           DECIMAL(20, 2) NOT NULL,
    finance_transaction_id BIGINT REFERENCES finance_transaction (id),




    calculation_note       TEXT,
    created_by             BIGINT REFERENCES users (id),
    created_at             TIMESTAMPTZ    DEFAULT (now() AT TIME ZONE 'utc')
);
--changeset asilbek:40
INSERT INTO roles (id, name)
VALUES (9, 'ROLE_HR')
ON CONFLICT (id) DO NOTHING;
SELECT setval('roles_id_seq', (SELECT COALESCE(MAX(id), 0) FROM roles), true);

--changeset asilbek:41
ALTER TABLE employees
    ADD COLUMN terminal_id BIGINT;

--changeset asilbek:42
ALTER TABLE attendance_records
    ADD COLUMN late_minutes        INT DEFAULT 0,
    ADD COLUMN early_leave_minutes INT DEFAULT 0,
    ADD COLUMN overtime_minutes    INT DEFAULT 0;
