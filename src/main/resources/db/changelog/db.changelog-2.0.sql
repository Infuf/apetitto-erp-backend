--liquibase formatted sql

--changeset asilbek:19
CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) UNIQUE,
    first_name VARCHAR(255),
    last_name  VARCHAR(255)
);

--changeset asilbek:20
CREATE TABLE roles
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);
--changeset asilbek:21
CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL REFERENCES users (id),
    role_id BIGINT NOT NULL REFERENCES roles (id),
    PRIMARY KEY (user_id, role_id)
);
--changeset asilbek:22
INSERT INTO roles (id, name)
VALUES (1, 'ROLE_ADMIN'),
       (2, 'ROLE_MANAGER'),
       (3, 'ROLE_USER')
ON CONFLICT (id) DO NOTHING;
SELECT setval('roles_id_seq', (SELECT COALESCE(MAX(id), 0) FROM roles), true);
