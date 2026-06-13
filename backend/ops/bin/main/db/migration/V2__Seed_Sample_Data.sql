-- Seed sample inventory items
INSERT INTO items (name, description, price, stock_quantity, low_stock_threshold, created_at, updated_at) VALUES
('Rice (5kg)', 'High-quality basmati rice', 250.00, 3, 10, NOW(), NOW()),
('Wheat Flour (2kg)', 'Refined wheat flour for baking', 120.00, 8, 15, NOW(), NOW()),
('Sugar (1kg)', 'Pure white sugar', 80.00, 25, 5, NOW(), NOW()),
('Oil (1L)', 'Premium cooking oil', 300.00, 12, 10, NOW(), NOW()),
('Salt (500g)', 'Iodized table salt', 40.00, 50, 20, NOW(), NOW()),
('Beans (1kg)', 'Dried red beans', 180.00, 2, 8, NOW(), NOW()),
('Lentils (1kg)', 'Yellow lentils', 160.00, 15, 5, NOW(), NOW()),
('Milk Powder (500g)', 'Fortified milk powder', 220.00, 20, 10, NOW(), NOW()),
('Biscuits (200g)', 'Chocolate cream biscuits', 100.00, 45, 15, NOW(), NOW()),
('Tea (250g)', 'Premium tea leaves', 350.00, 10, 5, NOW(), NOW());

-- The above seeds create a realistic inventory with some items below their low-stock threshold
-- Items below threshold: Rice (3 vs 10), Beans (2 vs 8)
