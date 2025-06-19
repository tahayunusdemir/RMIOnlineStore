package server;

import common.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSessionImpl extends UnicastRemoteObject implements IUserSession {

    private final Customer customer;
    // Stores the current user's shopping cart. The key is the Product ID, and the value is the quantity.
    private final Map<Integer, Integer> shoppingCart; // ProductID -> Quantity
    private final StoreFactoryImpl storeFactory;

    protected UserSessionImpl(Customer customer, StoreFactoryImpl storeFactory) throws RemoteException {
        super();
        this.customer = customer;
        this.shoppingCart = new HashMap<>();
        this.storeFactory = storeFactory;
    }

    @Override
    public synchronized List<Product> browseProducts() throws RemoteException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as categoryName FROM products p JOIN categories c ON p.categoryId = c.id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stockQuantity"),
                        rs.getString("categoryName"),
                        rs.getString("brand"),
                        rs.getString("size"),
                        rs.getString("color")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error browsing products.", e);
        }
        return products;
    }

    @Override
    public synchronized void addToCart(int productId, int quantity) throws RemoteException {
        if (quantity <= 0) {
            throw new RemoteException("Quantity must be positive.");
        }

        String sql = "SELECT stockQuantity FROM products WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int stock = rs.getInt("stockQuantity");
                    int currentCartQuantity = shoppingCart.getOrDefault(productId, 0);
                    if (stock >= quantity + currentCartQuantity) {
                        shoppingCart.put(productId, currentCartQuantity + quantity);
                        System.out.println("Product " + productId + " added to cart for customer " + customer.getUsername());
                    } else {
                        throw new RemoteException("Not enough stock for product ID: " + productId + ". Available: " + stock);
                    }
                } else {
                    throw new RemoteException("Product with ID " + productId + " not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error while adding to cart.", e);
        }
    }

    @Override
    public synchronized Map<Product, Integer> viewCart() throws RemoteException {
        Map<Product, Integer> detailedCart = new HashMap<>();
        if (shoppingCart.isEmpty()) {
            return detailedCart;
        }

        StringBuilder sqlBuilder = new StringBuilder("SELECT p.*, c.name as categoryName FROM products p JOIN categories c ON p.categoryId = c.id WHERE p.id IN (");
        for (int i = 0; i < shoppingCart.size(); i++) {
            sqlBuilder.append("?,");
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1).append(")");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {

            int i = 1;
            for (Integer productId : shoppingCart.keySet()) {
                pstmt.setInt(i++, productId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("stockQuantity"),
                            rs.getString("categoryName"),
                            rs.getString("brand"),
                            rs.getString("size"),
                            rs.getString("color")
                    );
                    detailedCart.put(product, shoppingCart.get(product.getId()));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error viewing cart.", e);
        }
        return detailedCart;
    }

    @Override
    public synchronized void removeFromCart(int productId) throws RemoteException {
        if (shoppingCart.containsKey(productId)) {
            shoppingCart.remove(productId);
            System.out.println("Product " + productId + " removed from cart for customer " + customer.getUsername());
        } else {
            System.err.println("Attempted to remove non-existent product " + productId + " from cart for " + customer.getUsername());
        }
    }

    @Override
    public synchronized void clearCart() throws RemoteException {
        if (!shoppingCart.isEmpty()) {
            shoppingCart.clear();
            System.out.println("Cart cleared for customer " + customer.getUsername());
        }
    }

    @Override
    public synchronized Order placeOrder() throws RemoteException {
        if (shoppingCart.isEmpty()) {
            throw new RemoteException("Shopping cart is empty.");
        }

        Connection conn = null;
        Order createdOrder = null;
        
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Start transaction: all or nothing.

            // 1. Verify stock for all items in the cart before proceeding.
            // This is a crucial check to ensure the order is valid. A real-world system might use row-level locking.
            for (Map.Entry<Integer, Integer> entry : shoppingCart.entrySet()) {
                String checkStockSql = "SELECT stockQuantity FROM products WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkStockSql)) {
                    ps.setInt(1, entry.getKey());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        if (rs.getInt("stockQuantity") < entry.getValue()) {
                            // Fetch product name for a more informative error message.
                            String productName = getProductName(conn, entry.getKey());
                            throw new SQLException("Not enough stock for product: " + productName + " (ID: " + entry.getKey() + ")");
                        }
                    } else {
                        throw new SQLException("Product not found with ID: " + entry.getKey());
                    }
                }
            }

            // 2. Create the main order record in the 'orders' table.
            String createOrderSql = "INSERT INTO orders (customerId, orderDate, totalAmount, status) VALUES (?, ?, ?, ?)";
            double totalAmount = calculateTotalAmount(conn);
            
            try(PreparedStatement psOrder = conn.prepareStatement(createOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, customer.getId());
                psOrder.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                psOrder.setDouble(3, totalAmount);
                psOrder.setString(4, Order.Status.PENDING.name());
                psOrder.executeUpdate();
                
                ResultSet generatedKeys = psOrder.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);

                    // 3. Add each item from the cart to the 'order_items' table and update the product stock.
                    String orderItemSql = "INSERT INTO order_items (orderId, productId, quantity, price) VALUES (?, ?, ?, ?)";
                    String updateStockSql = "UPDATE products SET stockQuantity = stockQuantity - ? WHERE id = ?";
                    
                    try(PreparedStatement psItem = conn.prepareStatement(orderItemSql);
                        PreparedStatement psUpdateStock = conn.prepareStatement(updateStockSql)) {

                        for (Map.Entry<Integer, Integer> entry : shoppingCart.entrySet()) {
                            // Get current price
                            double price = getProductPrice(conn, entry.getKey());

                            // Add to order_items
                            psItem.setInt(1, orderId);
                            psItem.setInt(2, entry.getKey());
                            psItem.setInt(3, entry.getValue());
                            psItem.setDouble(4, price);
                            psItem.addBatch();

                            // Update stock
                            psUpdateStock.setInt(1, entry.getValue());
                            psUpdateStock.setInt(2, entry.getKey());
                            psUpdateStock.addBatch();
                        }
                        psItem.executeBatch();
                        psUpdateStock.executeBatch();
                    }
                    
                    // Create the Order object to return to the client.
                    createdOrder = new Order(orderId, customer.getId(), new java.util.Date(), new ArrayList<>(), totalAmount, Order.Status.PENDING);

                }
            }
            
            conn.commit(); // If all steps were successful, commit the transaction to the database.
            System.out.println("Order placed successfully for customer: " + customer.getUsername());
            shoppingCart.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // If any SQL error occurs, roll back the entire transaction.
                    System.err.println("Transaction rolled back.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RemoteException("Error placing order: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Always restore auto-commit mode.
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return createdOrder;
    }
    
    private double calculateTotalAmount(Connection conn) throws SQLException {
        double total = 0;
        for (Map.Entry<Integer, Integer> entry : shoppingCart.entrySet()) {
            total += getProductPrice(conn, entry.getKey()) * entry.getValue();
        }
        return total;
    }

    private String getProductName(Connection conn, int productId) throws SQLException {
        String sql = "SELECT name FROM products WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
            return "Unknown Product";
        }
    }

    private double getProductPrice(Connection conn, int productId) throws SQLException {
        String sql = "SELECT price FROM products WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
            throw new SQLException("Product not found with ID: " + productId);
        }
    }

    @Override
    public synchronized List<Order> getOrderHistory() throws RemoteException {
        List<Order> orderHistory = new ArrayList<>();
        // This is a simplified query. A full implementation would also fetch order items.
        String sql = "SELECT * FROM orders WHERE customerId = ?";
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.customer.getId());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                orderHistory.add(new Order(
                    rs.getInt("id"),
                    rs.getInt("customerId"),
                    rs.getDate("orderDate"),
                    new ArrayList<>(), // Order items not fetched for simplicity
                    rs.getDouble("totalAmount"),
                    Order.Status.valueOf(rs.getString("status"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error fetching order history.", e);
        }
        return orderHistory;
    }

    @Override
    public void logout() throws RemoteException {
        // The factory handles the actual removal of the client's callback reference.
        // This method just signals the intent to log out.
        System.out.println("Customer " + customer.getUsername() + " logging out.");
        storeFactory.logout(customer.getUsername());
    }
} 