--liquibase formatted sql
--changeset your_name:seed_full_realistic_data logicalFilePath:full_seed_data.sql

-- =================================================================
-- ПОЛНЫЙ НАБОР ТЕСТОВЫХ ДАННЫХ ДЛЯ ФРОНТЕНД-ТЕСТИРОВАНИЯ
-- =================================================================

-- === 1. РОЛИ И ПОЛЬЗОВАТЕЛИ ===
-- Пароль для всех: 'password' (зашифрован через BCrypt)
INSERT INTO roles (id, name)
VALUES (1, 'ROLE_ADMIN'),
       (2, 'ROLE_MANAGER'),
       (3, 'ROLE_USER')
ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, username, password, email, first_name, last_name)
VALUES (1, 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqR2e5RzTTNpesY54Ea3o63zBs9K', 'admin@apetitto.com', 'Админ',
        'Админов'),
       (2, 'manager', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqR2e5RzTTNpesY54Ea3o63zBs9K', 'manager@apetitto.com',
        'Менеджер', 'Менеджеров'),
       (3, 'skladovshik', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqR2e5RzTTNpesY54Ea3o63zBs9K', 'sklad@apetitto.com', 'Али',
        'Складов'),
       (4, 'kassir', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqR2e5RzTTNpesY54Ea3o63zBs9K', 'kassa@apetitto.com', 'Вали',
        'Кассиров')
ON CONFLICT (id) DO NOTHING;
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1),
       (1, 2),
       (2, 2),
       (3, 2),
       (4, 3)
ON CONFLICT (user_id, role_id) DO NOTHING;


-- === 2. СКЛАДЫ И КАТЕГОРИИ ===
INSERT INTO warehouse (id, name, location, description)
VALUES (101, 'Основной склад (Сырье)', 'г. Коканд, ул. Туркистон, 1', 'Склад для хранения сырья и материалов'),
       (102, 'Склад готовой продукции', 'г. Коканд, м-в А.Навоий, 2', 'Склад для готовой продукции перед отправкой'),
       (103, 'Магазин "Центральный"', 'г. Коканд, ул. Истиклол, 25', 'Торговая точка №1'),
       (104, 'Филиал "Чорсу"', 'г. Коканд, рынок Чорсу', 'Торговая точка на рынке')
ON CONFLICT (id) DO NOTHING;

INSERT INTO category (id, name, description)
VALUES (201, 'Молочные продукты', 'Сыры, молоко, кефир, йогурты'),
       (202, 'Хлебобулочные изделия', 'Хлеб, выпечка, лепешки, булочки'),
       (203, 'Бакалея', 'Крупы, макароны, мука, консервы'),
       (204, 'Напитки', 'Соки, вода, лимонады'),
       (205, 'Мясные продукты', 'Говядина, баранина, курица, колбасы'),
       (206, 'Овощи и фрукты', 'Сезонные овощи и фрукты'),
       (207, 'Замороженные продукты', 'Полуфабрикаты и овощи замороженные'),
       (208, 'Соусы и приправы', 'Майонез, кетчуп, соусы, специи'),
       (209, 'Кондитерские изделия', 'Шоколад, печенье, торты'),
       (210, 'Под удаление', 'Категория для теста на удаление')
ON CONFLICT (id) DO NOTHING;


-- === 3. ТОВАРЫ (40+ позиций) ===
INSERT INTO product (id, product_code, name, unit, category_id, selling_price)
VALUES (301, 'PROD-001', 'Нон (Лепешка)', 'PIECE', 202, 3000.00),
       (302, 'PROD-002', 'Буханка хлеба', 'PIECE', 202, 2500.00),
       (303, 'PROD-003', 'Самса с мясом', 'PIECE', 202, 5000.00),
       (304, 'PROD-004', 'Мука "Олий Нон" 2кг', 'PIECE', 203, 18000.00),
       (305, 'PROD-005', 'Сахар 1кг', 'PIECE', 203, 13000.00),
       (306, 'PROD-006', 'Рис "Лазер"', 'KILOGRAM', 203, 25000.00),
       (307, 'PROD-007', 'Масло хлопковое "Мехнат" 1л', 'PIECE', 203, 22000.00),
       (308, 'PROD-008', 'Гречка', 'KILOGRAM', 203, 15000.00),
       (309, 'PROD-009', 'Молоко "Мусаффо" 1л', 'PIECE', 201, 9000.00),
       (310, 'PROD-010', 'Кефир "Камилка" 1л', 'PIECE', 201, 8500.00),
       (311, 'PROD-011', 'Сметана', 'KILOGRAM', 201, 30000.00),
       (312, 'PROD-012', 'Сыр "Голландский"', 'KILOGRAM', 201, 80000.00),
       (313, 'PROD-013', 'Сок "Bliss" яблочный 1л', 'PIECE', 204, 14000.00),
       (314, 'PROD-014', 'Вода "Nestle" 1.5л', 'PIECE', 204, 4000.00),
       (315, 'PROD-015', 'Coca-Cola 1.5л', 'PIECE', 204, 12000.00),
       (316, 'PROD-016', 'Говядина (вырезка)', 'KILOGRAM', 205, 90000.00),
       (317, 'PROD-017', 'Курица (тушка)', 'KILOGRAM', 205, 35000.00),
       (318, 'PROD-018', 'Картофель', 'KILOGRAM', 206, 4000.00),
       (319, 'PROD-019', 'Лук репчатый', 'KILOGRAM', 206, 3000.00),
       (320, 'PROD-020', 'Помидоры "Юсуповские"', 'KILOGRAM', 206, 15000.00),
       (321, 'PROD-021', 'Пельмени "Домашние"', 'KILOGRAM', 207, 36000.00),
       (322, 'PROD-022', 'Майонез "Домашний" 800г', 'PIECE', 208, 15000.00),
       (323, 'PROD-023', 'Кетчуп 350г', 'PIECE', 208, 7000.00),
       (324, 'PROD-024', 'Шоколад "Alpen Gold"', 'PIECE', 209, 12000.00),
       (325, 'PROD-025', 'Яйцо куриное С1 (10шт)', 'PIECE', 201, 14000.00),
       (326, 'PROD-026', 'Масло сливочное 200г', 'PIECE', 201, 26000.00),
       (327, 'PROD-027', 'Колбаса "Докторская"', 'KILOGRAM', 205, 52000.00),
       (328, 'PROD-028', 'Оливковое масло 1л', 'PIECE', 203, 140000.00),
       (329, 'PROD-029', 'Соус соевый 500мл', 'PIECE', 208, 18000.00),
       (330, 'PROD-030', 'Перец молотый 50г', 'PIECE', 208, 6000.00),
       (331, 'PROD-031', 'Товар без остатка', 'PIECE', 209, 1000.00)
ON CONFLICT (id) DO NOTHING;


-- === 4. НАЧАЛЬНЫЕ ОСТАТКИ И ДВИЖЕНИЯ ===
-- Создаем движения и соответствующие им остатки.
-- Движение №1: Большое поступление на Основной склад (101)
INSERT INTO stock_movement (id, warehouse_id, movement_type, comment)
VALUES (1, 101, 'INBOUND', 'Первоначальное оприходование');
INSERT INTO stock_movement_item (movement_id, product_id, quantity, cost_price)
VALUES (1, 304, 200.0, 14000.00),
       (1, 305, 500.0, 10000.00),
       (1, 306, 1000.0, 18000.00),
       (1, 307, 300.0, 18000.00),
       (1, 308, 500.0, 11000.00),
       (1, 316, 50.0, 75000.00),
       (1, 317, 100.0, 28000.00),
       (1, 318, 800.0, 2500.00),
       (1, 319, 1000.0, 2000.00),
       (1, 320, 150.0, 10000.00);
INSERT INTO stock_item (warehouse_id, product_id, quantity, average_cost)
VALUES (101, 304, 200.0, 14000.00),
       (101, 305, 500.0, 10000.00),
       (101, 306, 1000.0, 18000.00),
       (101, 307, 300.0, 18000.00),
       (101, 308, 500.0, 11000.00),
       (101, 316, 50.0, 75000.00),
       (101, 317, 100.0, 28000.00),
       (101, 318, 800.0, 2500.00),
       (101, 319, 1000.0, 2000.00),
       (101, 320, 150.0, 10000.00);

-- Движение №2: Поступление в Магазин "Центральный" (103)
INSERT INTO stock_movement (id, warehouse_id, movement_type, comment)
VALUES (2, 103, 'INBOUND', 'Первоначальное оприходование магазина');
INSERT INTO stock_movement_item (movement_id, product_id, quantity, cost_price)
VALUES (2, 301, 100.0, 2200.00),
       (2, 302, 80.0, 1800.00),
       (2, 303, 150.0, 3500.00),
       (2, 309, 50.0, 7000.00),
       (2, 310, 40.0, 6800.00),
       (2, 313, 60.0, 11000.00),
       (2, 314, 120.0, 2500.00),
       (2, 315, 90.0, 9500.00);
INSERT INTO stock_item (warehouse_id, product_id, quantity, average_cost)
VALUES (103, 301, 100.0, 2200.00),
       (103, 302, 80.0, 1800.00),
       (103, 303, 150.0, 3500.00),
       (103, 309, 50.0, 7000.00),
       (103, 310, 40.0, 6800.00),
       (103, 313, 60.0, 11000.00),
       (103, 314, 120.0, 2500.00),
       (103, 315, 90.0, 9500.00);

-- Движение №3: Продажа из Магазина "Центральный" (103)
INSERT INTO stock_movement (id, warehouse_id, movement_type, comment)
VALUES (3, 103, 'OUTBOUND', 'Продажа #1');
INSERT INTO stock_movement_item (movement_id, product_id, quantity, cost_price)
VALUES (3, 301, 20.0, null),
       (3, 315, 10.0, null);
UPDATE stock_item
SET quantity = quantity - 20.0
WHERE warehouse_id = 103
  AND product_id = 301;
UPDATE stock_item
SET quantity = quantity - 10.0
WHERE warehouse_id = 103
  AND product_id = 315;

-- Движение №4: Корректировка на Основном складе (101)
INSERT INTO stock_movement (id, warehouse_id, movement_type, comment)
VALUES (4, 101, 'ADJUSTMENT', 'Инвентаризация: списание испорченного картофеля');
INSERT INTO stock_movement_item (movement_id, product_id, quantity, cost_price)
VALUES (4, 318, -50.0, null);
UPDATE stock_item
SET quantity = quantity - 50.0
WHERE warehouse_id = 101
  AND product_id = 318;


-- === 5. ПЕРЕМЕЩЕНИЯ (3 примера) ===
-- Пример 1: Отправлено и получено
INSERT INTO transfer_order (id, source_warehouse_id, destination_warehouse_id, status, shipped_at, received_at)
VALUES (1, 101, 103, 'RECEIVED', now() - interval '5 days', now() - interval '4 days');
INSERT INTO transfer_order_item (transfer_order_id, product_id, quantity, cost_at_transfer)
VALUES (1, 325, 30.0, 12000.00);
INSERT INTO stock_movement (id, warehouse_id, movement_type, comment)
VALUES (5, 101, 'TRANSFER_OUT', 'Перемещение #1 в Магазин'),
       (6, 103, 'TRANSFER_IN', 'Приемка #1 с Основного склада');
INSERT INTO stock_movement_item (movement_id, product_id, quantity, cost_price)
VALUES (5, 325, 30.0, null),
       (6, 325, 30.0, 12000.00);
INSERT INTO stock_item (warehouse_id, product_id, quantity, average_cost)
VALUES (103, 325, 30.0, 12000.00);
-- Предполагаем, что на складе 101 было достаточно

-- Пример 2: Только отправлено (в пути)
INSERT INTO transfer_order (id, source_warehouse_id, destination_warehouse_id, status, shipped_at)
VALUES (2, 101, 104, 'SHIPPED', now() - interval '1 day');
INSERT INTO transfer_order_item (transfer_order_id, product_id, quantity, cost_at_transfer)
VALUES (2, 326, 100.0, 24000.00);
INSERT INTO stock_movement (id, warehouse_id, movement_type, comment)
VALUES (7, 101, 'TRANSFER_OUT', 'Перемещение #2 в Филиал Чорсу');
INSERT INTO stock_movement_item (movement_id, product_id, quantity, cost_price)
VALUES (7, 326, 100.0, null);
INSERT INTO stock_item (warehouse_id, product_id, quantity, average_cost)
VALUES (101, 326, 100.0, 24000.00); -- Предполагаем, что это было первое поступление на склад 101
UPDATE stock_item
SET quantity = quantity - 100.0
WHERE warehouse_id = 101
  AND product_id = 326;

-- Пример 3: Только создан (ожидает отправки)
INSERT INTO transfer_order (id, source_warehouse_id, destination_warehouse_id, status)
VALUES (3, 102, 103, 'PENDING');
INSERT INTO transfer_order_item (transfer_order_id, product_id, quantity, cost_at_transfer)
VALUES (3, 301, 5.0, 0.00);


-- === 6. ОБНОВЛЕНИЕ ПОСЛЕДОВАТЕЛЬНОСТЕЙ ===
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 0) FROM users), true);
SELECT setval('roles_id_seq', (SELECT COALESCE(MAX(id), 0) FROM roles), true);
SELECT setval('warehouse_id_seq', (SELECT COALESCE(MAX(id), 0) FROM warehouse), true);
SELECT setval('category_id_seq', (SELECT COALESCE(MAX(id), 0) FROM category), true);
SELECT setval('product_id_seq', (SELECT COALESCE(MAX(id), 0) FROM product), true);
SELECT setval('stock_item_id_seq', (SELECT COALESCE(MAX(id), 0) FROM stock_item), true);
SELECT setval('stock_movement_id_seq', (SELECT COALESCE(MAX(id), 0) FROM stock_movement), true);
SELECT setval('transfer_order_id_seq', (SELECT COALESCE(MAX(id), 0) FROM transfer_order), true);