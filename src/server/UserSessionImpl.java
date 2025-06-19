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
        // In a real scenario, you should check stock before adding
        shoppingCart.put(productId, shoppingCart.getOrDefault(productId, 0) + quantity);
        System.out.println("Product " + productId + " added to cart for customer " + customer.getUsername());
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
    public synchronized Order placeOrder() throws RemoteException {
        if (shoppingCart.isEmpty()) {
            throw new RemoteException("Shopping cart is empty.");
        }

        Connection conn = null;
        Order createdOrder = null;
        
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Check stock for all items
            // This part is simplified. A real implementation should lock the rows to prevent race conditions.
            for (Map.Entry<Integer, Integer> entry : shoppingCart.entrySet()) {
                String checkStockSql = "SELECT stockQuantity FROM products WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkStockSql)) {
                    ps.setInt(1, entry.getKey());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        if (rs.getInt("stockQuantity") < entry.getValue()) {
                            throw new SQLException("Not enough stock for product ID: " + entry.getKey());
                        }
                    } else {
                        throw new SQLException("Product not found with ID: " + entry.getKey());
                    }
                }
            }

            // 2. Create the order
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

                    // 3. Add order items and update stock
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
                    
                    // Create Order object to return
                    createdOrder = new Order(orderId, customer.getId(), new java.util.Date(), new ArrayList<>(), totalAmount, Order.Status.PENDING);

                }
            }
            
            conn.commit(); // Commit transaction
            System.out.println("Order placed successfully for customer: " + customer.getUsername());
            shoppingCart.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                    System.err.println("Transaction rolled back.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RemoteException("Error placing order: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
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
        // The factory will handle the actual removal of the session
        System.out.println("Customer " + customer.getUsername() + " logging out.");
        storeFactory.logout(customer.getUsername());
    }
} 