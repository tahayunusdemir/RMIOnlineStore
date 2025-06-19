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
