INSERT INTO roles (id, name)
VALUES (1, 'ROLE_ADMIN'),
       (2, 'ROLE_MANAGER'),
       (3, 'ROLE_USER')
ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, username, password, email, first_name, last_name)
VALUES (1, 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqR2e5RzTTNpesY54Ea3o63zBs9K', 'admin@test.com', 'Админ',
        'Админов'),
       (2, 'manager', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqR2e5RzTTNpesY54Ea3o63zBs9K', 'manager@test.com', 'Менеджер',
        'Менеджеров')
ON CONFLICT (id) DO NOTHING;
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1),
       (1, 2),
       (2, 2)
ON CONFLICT (user_id, role_id) DO NOTHING;



INSERT INTO warehouse (id, name, location)
VALUES (101, 'Тестовый Основной склад', 'г. Коканд'),
       (102, 'Тестовый Магазин "Центральный"', 'г. Коканд'),
       (103, 'Тестовый Магазин "Южный"', 'г. Коканд')
ON CONFLICT (id) DO NOTHING;

INSERT INTO category (id, name)
VALUES (201, 'Молочные продукты'),
       (202, 'Хлебобулочные изделия'),
       (203, 'Бакалея'),
       (204, 'Напитки'),
       (205, 'Под удаление')
ON CONFLICT (id) DO NOTHING;


INSERT INTO product (id, product_code, name, unit, category_id, selling_price)
VALUES (301, 'PROD-TEST-001', 'Нон (Лепешка)', 'PIECE', 202, 3000.00),
       (302, 'PROD-TEST-002', 'Рис "Лазер"', 'KILOGRAM', 203, 25000.00),
       (303, 'PROD-TEST-003', 'Молоко "Мусаффо" 1л', 'PIECE', 201, 9000.00),
       (304, 'PROD-TEST-004', 'Coca-Cola 1.5л', 'PIECE', 204, 12000.00),
       (305, 'PROD-TEST-005', 'Говядина (вырезка)', 'KILOGRAM', 201,
        90000.00) -- Добавим товар, которого нет на остатках
ON CONFLICT (id) DO NOTHING;



INSERT INTO stock_item (id, warehouse_id, product_id, quantity, average_cost)
VALUES (401, 101, 301, 50, 2200.0000),
       (402, 101, 302, 1000, 18000.0000),
       (403, 101, 303, 200, 7000.0000)
ON CONFLICT (id) DO NOTHING;


INSERT INTO stock_item (id, warehouse_id, product_id, quantity, average_cost)
VALUES (404, 102, 301, 20, 2300.0000),
       (405, 102, 304, 150, 9500.0000)
ON CONFLICT (id) DO NOTHING;



SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 0) FROM users), true);
SELECT setval('roles_id_seq', (SELECT COALESCE(MAX(id), 0) FROM roles), true);
SELECT setval('warehouse_id_seq', (SELECT COALESCE(MAX(id), 0) FROM warehouse), true);
SELECT setval('category_id_seq', (SELECT COALESCE(MAX(id), 0) FROM category), true);
SELECT setval('product_id_seq', (SELECT COALESCE(MAX(id), 0) FROM product), true);
SELECT setval('stock_item_id_seq', (SELECT COALESCE(MAX(id), 0) FROM stock_item), true);