package server;

import common.IAdminPanel;
import common.Product;
import common.Order;
import common.Category;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminPanelImpl extends UnicastRemoteObject implements IAdminPanel {

    private final StoreFactoryImpl storeFactory;

    // A reference to the main data store might be needed here
    // For example: private final DataStore dataStore;

    protected AdminPanelImpl(StoreFactoryImpl storeFactory) throws RemoteException {
        super();
        this.storeFactory = storeFactory;
    }

    @Override
    public synchronized void addProduct(Product product) throws RemoteException {
        String sql = "INSERT INTO products (name, description, price, stockQuantity, categoryId, brand, size, color) VALUES (?, ?, ?, ?, (SELECT id FROM categories WHERE name = ?), ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getStockQuantity());
            pstmt.setString(5, product.getCategory());
            pstmt.setString(6, product.getBrand());
            pstmt.setString(7, product.getSize());
            pstmt.setString(8, product.getColor());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Product added successfully: " + product.getName());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error while adding product.", e);
        }
    }

    @Override
    public synchronized void updateStock(int productId, int newQuantity) throws RemoteException {
        String sql = "UPDATE products SET stockQuantity = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, productId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Stock updated for product ID " + productId);
            } else {
                System.out.println("Product with ID " + productId + " not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error while updating stock.", e);
        }
    }

    @Override
    public synchronized String getStatistics() throws RemoteException {
        StringBuilder stats = new StringBuilder();
        String totalCustomersSql = "SELECT COUNT(*) FROM customers";
        String totalProductsSql = "SELECT COUNT(*) FROM products";
        String totalOrdersSql = "SELECT COUNT(*) FROM orders";
        String totalRevenueSql = "SELECT SUM(totalAmount) FROM orders WHERE status = 'DELIVERED'";

        try (Connection conn = DatabaseManager.getConnection()) {
            // Get total customers
            try (PreparedStatement pstmt = conn.prepareStatement(totalCustomersSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.append("Total Registered Customers: ").append(rs.getInt(1)).append("\n");
                }
            }

            // Get total products
            try (PreparedStatement pstmt = conn.prepareStatement(totalProductsSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.append("Total Products in Catalog: ").append(rs.getInt(1)).append("\n");
                }
            }

            // Get total orders
            try (PreparedStatement pstmt = conn.prepareStatement(totalOrdersSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.append("Total Orders Placed: ").append(rs.getInt(1)).append("\n");
                }
            }
            
            // Get total revenue
            try (PreparedStatement pstmt = conn.prepareStatement(totalRevenueSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.append("Total Revenue (from delivered orders): $").append(String.format("%.2f", rs.getDouble(1))).append("\n");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error while generating statistics.", e);
        }

        System.out.println("Generating statistics...");
        return stats.toString();
    }

    @Override
    public synchronized List<Product> browseProducts() throws RemoteException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as categoryName FROM products p LEFT JOIN categories c ON p.categoryId = c.id";
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
    public synchronized void updateOrderStatus(int orderId, Order.Status newStatus) throws RemoteException {
        String getUsernameSql = "SELECT c.username FROM customers c JOIN orders o ON c.id = o.customerId WHERE o.id = ?";
        String updateStatusSql = "UPDATE orders SET status = ? WHERE id = ?";
        String username = null;

        try (Connection conn = DatabaseManager.getConnection()) {
            // Get username for notification
            try (PreparedStatement pstmt = conn.prepareStatement(getUsernameSql)) {
                pstmt.setInt(1, orderId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        username = rs.getString("username");
                    } else {
                        throw new RemoteException("Order with ID " + orderId + " not found.");
                    }
                }
            }

            // Update order status
            try (PreparedStatement pstmt = conn.prepareStatement(updateStatusSql)) {
                pstmt.setString(1, newStatus.name());
                pstmt.setInt(2, orderId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Order " + orderId + " status updated to " + newStatus);
                    // Notify the client if they are online
                    if (username != null) {
                        String message = "The status of your order #" + orderId + " has been updated to: " + newStatus;
                        storeFactory.notifyClient(username, message);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error while updating order status.", e);
        }
    }

    @Override
    public synchronized List<Order> viewAllOrders() throws RemoteException {
        List<Order> allOrders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY orderDate DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while(rs.next()){
                allOrders.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("customerId"),
                        rs.getTimestamp("orderDate"),
                        new ArrayList<>(), // Order items not fetched for simplicity
                        rs.getDouble("totalAmount"),
                        Order.Status.valueOf(rs.getString("status"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error fetching all orders.", e);
        }
        return allOrders;
    }

    @Override
    public synchronized void addCategory(String categoryName) throws RemoteException {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryName);
            pstmt.executeUpdate();
            System.out.println("Category added successfully: " + categoryName);
        } catch (SQLException e) {
            // SQL state '23000' is for integrity constraint violation (e.g., duplicate entry)
            if (e.getSQLState().startsWith("23")) {
                throw new RemoteException("Category '" + categoryName + "' already exists.", e);
            }
            e.printStackTrace();
            throw new RemoteException("Database error while adding category.", e);
        }
    }

    @Override
    public synchronized List<Category> getAllCategories() throws RemoteException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while(rs.next()){
                categories.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error fetching categories.", e);
        }
        return categories;
    }

    @Override
    public synchronized void deleteProduct(int productId) throws RemoteException {
        // Check if the product is in any orders
        String checkOrdersSql = "SELECT COUNT(*) FROM order_items WHERE productId = ?";
        String deleteProductSql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            // Check for existing orders
            try (PreparedStatement pstmt = conn.prepareStatement(checkOrdersSql)) {
                pstmt.setInt(1, productId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new RemoteException("Cannot delete product ID " + productId + ". It is part of existing orders.");
                }
            }

            // If no orders, proceed with deletion
            try (PreparedStatement pstmt = conn.prepareStatement(deleteProductSql)) {
                pstmt.setInt(1, productId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Product with ID " + productId + " deleted successfully.");
                } else {
                    throw new RemoteException("Product with ID " + productId + " not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error while deleting product.", e);
        }
    }

    @Override
    public synchronized void deleteCategory(int categoryId) throws RemoteException {
        // Check if the category is used by any products
        String checkProductsSql = "SELECT COUNT(*) FROM products WHERE categoryId = ?";
        String deleteCategorySql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            // Check for products in this category
            try (PreparedStatement pstmt = conn.prepareStatement(checkProductsSql)) {
                pstmt.setInt(1, categoryId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new RemoteException("Cannot delete category ID " + categoryId + ". It is currently assigned to " + rs.getInt(1) + " products.");
                }
            }

            // If no products, proceed with deletion
            try (PreparedStatement pstmt = conn.prepareStatement(deleteCategorySql)) {
                pstmt.setInt(1, categoryId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Category with ID " + categoryId + " deleted successfully.");
                } else {
                    throw new RemoteException("Category with ID " + categoryId + " not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error while deleting category.", e);
        }
    }
} 