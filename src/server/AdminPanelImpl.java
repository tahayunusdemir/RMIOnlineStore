package server;

import common.IAdminPanel;
import common.Product;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdminPanelImpl extends UnicastRemoteObject implements IAdminPanel {

    // A reference to the main data store might be needed here
    // For example: private final DataStore dataStore;

    protected AdminPanelImpl() throws RemoteException {
        super();
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
        // TODO: Generate and return statistics from database
        System.out.println("Generating statistics...");
        return "Statistics report (Not implemented yet).";
    }
} 