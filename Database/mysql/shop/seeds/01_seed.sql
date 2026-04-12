INSERT INTO admins (id, email, password_hash, name, status) VALUES
  (1, 'admin-shop-1@example.com', 'hashed-shop-admin', 'Shop Admin', 'ACTIVE');

INSERT INTO users (id, email, password_hash, name, phone, status) VALUES
  (1, 'shop-user-1@example.com', 'hashed-shop-user-1', 'Alice Kim', '010-1111-1111', 'ACTIVE'),
  (2, 'shop-user-2@example.com', 'hashed-shop-user-2', 'Bob Lee', '010-2222-2222', 'ACTIVE'),
  (3, 'shop-user-3@example.com', 'hashed-shop-user-3', 'Chris Park', '010-3333-3333', 'ACTIVE');

INSERT INTO categories (id, name, display_order, status) VALUES
  (1, 'keyboard', 1, 'ACTIVE'),
  (2, 'mouse', 2, 'ACTIVE'),
  (3, 'monitor', 3, 'ACTIVE');

INSERT INTO products (id, category_id, name, description, price, stock, status) VALUES
  (1, 1, 'PB Mechanical Keyboard', '기계식 키보드 샘플 상품', 129000.00, 50, 'ACTIVE'),
  (2, 2, 'PB Wireless Mouse', '무선 마우스 샘플 상품', 59000.00, 80, 'ACTIVE'),
  (3, 3, 'PB 27 Monitor', '27인치 모니터 샘플 상품', 329000.00, 20, 'ACTIVE'),
  (4, 1, 'PB Compact Keyboard', '컴팩트 키보드 샘플 상품', 99000.00, 35, 'HIDDEN');

INSERT INTO product_options (id, product_id, name, value, additional_price, stock) VALUES
  (1, 1, 'switch', 'red', 0.00, 20),
  (2, 1, 'switch', 'brown', 5000.00, 15),
  (3, 2, 'color', 'black', 0.00, 40),
  (4, 3, 'size', '27inch', 0.00, 20);

INSERT INTO product_images (id, product_id, image_url, is_primary, display_order) VALUES
  (1, 1, 'https://example.com/images/keyboard-main.jpg', TRUE, 1),
  (2, 1, 'https://example.com/images/keyboard-side.jpg', FALSE, 2),
  (3, 2, 'https://example.com/images/mouse-main.jpg', TRUE, 1),
  (4, 3, 'https://example.com/images/monitor-main.jpg', TRUE, 1);

INSERT INTO addresses (id, user_id, recipient_name, phone, zip_code, address1, address2, is_default) VALUES
  (1, 1, 'Alice Kim', '010-1111-1111', '06234', 'Seoul Road 1', '101-1201', TRUE),
  (2, 2, 'Bob Lee', '010-2222-2222', '13579', 'Busan Road 2', '202-303', TRUE),
  (3, 3, 'Chris Park', '010-3333-3333', '24680', 'Incheon Road 3', '303-404', TRUE);

INSERT INTO cart_items (id, user_id, product_id, product_option_id, quantity) VALUES
  (1, 1, 1, 2, 1),
  (2, 1, 2, 3, 2),
  (3, 2, 3, 4, 1);

INSERT INTO orders (id, user_id, order_number, order_status, total_amount, payment_status, ordered_at) VALUES
  (1, 1, 'ORD-20260412-0001', 'PAID', 134000.00, 'PAID', CURRENT_TIMESTAMP),
  (2, 2, 'ORD-20260412-0002', 'DELIVERED', 329000.00, 'PAID', CURRENT_TIMESTAMP);

INSERT INTO order_addresses (id, order_id, recipient_name, phone, zip_code, address1, address2) VALUES
  (1, 1, 'Alice Kim', '010-1111-1111', '06234', 'Seoul Road 1', '101-1201'),
  (2, 2, 'Bob Lee', '010-2222-2222', '13579', 'Busan Road 2', '202-303');

INSERT INTO order_items (id, order_id, product_id, product_option_id, product_name_snapshot, option_name_snapshot, unit_price, quantity, line_amount) VALUES
  (1, 1, 1, 2, 'PB Mechanical Keyboard', 'switch:brown', 134000.00, 1, 134000.00),
  (2, 2, 3, 4, 'PB 27 Monitor', 'size:27inch', 329000.00, 1, 329000.00);

INSERT INTO payments (id, order_id, payment_method, payment_status, paid_amount, paid_at) VALUES
  (1, 1, 'mock-card', 'PAID', 134000.00, CURRENT_TIMESTAMP),
  (2, 2, 'mock-card', 'PAID', 329000.00, CURRENT_TIMESTAMP);

INSERT INTO reviews (id, order_item_id, product_id, user_id, rating, content, status) VALUES
  (1, 2, 3, 2, 5, '모니터 품질이 좋아요.', 'ACTIVE');
