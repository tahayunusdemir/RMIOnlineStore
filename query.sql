-- 1. Create the database (if it does not already exist)
CREATE DATABASE IF NOT EXISTS rmi_onlinestore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Use the created database
USE rmi_onlinestore;

-- 3. Customers table
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Always store passwords as hashes!
    name VARCHAR(100) NOT NULL,
    address TEXT
);

-- 4. Categories table
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 5. Products table
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stockQuantity INT NOT NULL,
    categoryId INT,
    brand VARCHAR(100),
    size VARCHAR(20),
    color VARCHAR(50),
    FOREIGN KEY (categoryId) REFERENCES categories(id)
);

-- 6. Orders table
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customerId INT NOT NULL,
    orderDate DATETIME NOT NULL,
    totalAmount DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'SHIPPED', 'DELIVERED') NOT NULL,
    FOREIGN KEY (customerId) REFERENCES customers(id)
);

-- 7. Order items table (Keeps track of which products and how many are in each order)
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orderId INT NOT NULL,
    productId INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL, -- It is important to store the price at the time of purchase
    FOREIGN KEY (orderId) REFERENCES orders(id),
    FOREIGN KEY (productId) REFERENCES products(id)
);

-- Let's add a sample category
INSERT INTO categories (name) VALUES ('Running Shoes') ON DUPLICATE KEY UPDATE name=name;

-- Let's add another sample category
INSERT INTO categories (name) VALUES ('Football Jerseys') ON DUPLICATE KEY UPDATE name=name;

-- Let's add some sample products
INSERT INTO products (name, description, price, stockQuantity, categoryId, brand, size, color) VALUES
('Nike Air Zoom Pegasus 40', 'A responsive ride for any run.', 129.99, 50, (SELECT id FROM categories WHERE name = 'Running Shoes'), 'Nike', '42', 'Black'),
('Adidas Ultraboost 22', 'Ultimate energy return and comfort.', 180.00, 30, (SELECT id FROM categories WHERE name = 'Running Shoes'), 'Adidas', '43', 'White'),
('FC Barcelona 23/24 Home Jersey', 'Official home jersey for the 23/24 season.', 95.50, 100, (SELECT id FROM categories WHERE name = 'Football Jerseys'), 'Nike', 'L', 'Blue/Red');

-- Let's add more categories
INSERT INTO categories (name) VALUES ('Basketball Gear'), ('Fitness Apparel') ON DUPLICATE KEY UPDATE name=name;

-- Let's add more products
INSERT INTO products (name, description, price, stockQuantity, categoryId, brand, size, color) VALUES
('Spalding NBA Street Basketball', 'Official size and weight street basketball.', 24.99, 150, (SELECT id FROM categories WHERE name = 'Basketball Gear'), 'Spalding', '7', 'Orange'),
('Nike Dri-FIT Training T-Shirt', 'Sweat-wicking fabric to keep you dry.', 35.00, 200, (SELECT id FROM categories WHERE name = 'Fitness Apparel'), 'Nike', 'M', 'Grey'),
('Real Madrid 23/24 Away Jersey', 'Official away jersey for the 23/24 season.', 95.50, 80, (SELECT id FROM categories WHERE name = 'Football Jerseys'), 'Adidas', 'M', 'Navy');

-- Let's add some sample customers (passwords are 'password123' and 'securepass')
INSERT INTO customers (username, password, name, address) VALUES
('alice', 'password123', 'Alice Smith', '456 Oak Ave, Othertown'),
('bob', 'securepass', 'Bob Johnson', '789 Pine St, Anotherville')
ON DUPLICATE KEY UPDATE username=username;

-- Let's create a sample order for Alice
INSERT INTO orders (customerId, orderDate, totalAmount, status) VALUES
((SELECT id FROM customers WHERE username = 'alice'), '2024-05-10 10:30:00', 164.98, 'DELIVERED');

-- Let's add items to Alice's order
INSERT INTO order_items (orderId, productId, quantity, price) VALUES
((SELECT id FROM orders WHERE customerId = (SELECT id FROM customers WHERE username = 'alice') LIMIT 1), (SELECT id FROM products WHERE name LIKE 'Nike Air Zoom%'), 1, 129.99),
((SELECT id FROM orders WHERE customerId = (SELECT id FROM customers WHERE username = 'alice') LIMIT 1), (SELECT id FROM products WHERE name LIKE 'Spalding%'), 1, 24.99);

-- Let's create a sample order for Bob
INSERT INTO orders (customerId, orderDate, totalAmount, status) VALUES
((SELECT id FROM customers WHERE username = 'bob'), '2024-05-20 14:00:00', 35.00, 'SHIPPED');

-- Let's add an item to Bob's order
INSERT INTO order_items (orderId, productId, quantity, price) VALUES
((SELECT id FROM orders WHERE customerId = (SELECT id FROM customers WHERE username = 'bob') LIMIT 1), (SELECT id FROM products WHERE name LIKE 'Nike Dri-FIT%'), 1, 35.00);

-- Let's create a recent PENDING order for Alice to test status updates
INSERT INTO orders (customerId, orderDate, totalAmount, status) VALUES
((SELECT id FROM customers WHERE username = 'alice'), NOW(), 95.50, 'PENDING');

INSERT INTO order_items (orderId, productId, quantity, price) VALUES
(LAST_INSERT_ID(), (SELECT id FROM products WHERE name LIKE 'Real Madrid%'), 1, 95.50);
