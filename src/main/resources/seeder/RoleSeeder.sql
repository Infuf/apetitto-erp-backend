--changeset asilbek:22
INSERT INTO roles (name)
VALUES ('ROLE_ADMIN'),
       ('ROLE_MANAGER'),
       ('ROLE_USER')
ON CONFLICT (name) DO NOTHING;