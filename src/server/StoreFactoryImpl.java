package server;

import common.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StoreFactoryImpl extends UnicastRemoteObject implements IStoreFactory {

    // A thread-safe map to store callback references for all currently active (logged-in) clients.
    // The key is the client's username, and the value is their callback object.
    private final Map<String, IClientCallback> activeClients;

    public StoreFactoryImpl() throws RemoteException {
        super();
        activeClients = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized IUserSession login(String username, String password, IClientCallback clientCallback) throws RemoteException {
        String sql = "SELECT * FROM customers WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("name"),
                            rs.getString("address")
                    );
                    System.out.println("Customer login successful: " + username);
                    // If login succeeds, store the client's callback reference for future notifications.
                    activeClients.put(username, clientCallback);
                    return new UserSessionImpl(customer, this);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error during login.", e);
        }
        System.out.println("Customer login failed: " + username);
        return null;
    }

    @Override
    public synchronized void registerCustomer(Customer newCustomer) throws RemoteException {
        String sql = "INSERT INTO customers (username, password, name, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (customerExists(conn, newCustomer.getUsername())) {
                throw new RemoteException("Username already exists.");
            }

            pstmt.setString(1, newCustomer.getUsername());
            pstmt.setString(2, newCustomer.getPassword());
            pstmt.setString(3, newCustomer.getName());
            pstmt.setString(4, newCustomer.getAddress());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("New customer registered: " + newCustomer.getUsername());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error during registration.", e);
        }
    }

    private boolean customerExists(Connection conn, String username) throws SQLException {
        String sql = "SELECT id FROM customers WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, a customer with that username was found.
                return rs.next();
            }
        }
    }

    @Override
    public synchronized IAdminPanel adminLogin(String username, String password) throws RemoteException {
        // Hardcoded credentials for admin access.
        if ("admin".equals(username) && "admin".equals(password)) {
            System.out.println("Admin login successful: " + username);
            return new AdminPanelImpl(this);
        }
        System.out.println("Admin login failed: " + username);
        return null;
    }

    @Override
    public void logout(String username) throws RemoteException {
        if (username != null) {
            // Remove the client from the active list upon logout to stop sending notifications.
            activeClients.remove(username);
            System.out.println("Client " + username + " removed from active clients list.");
        }
    }

    // Method to notify all active clients.
    public void notifyClients(String message) {
        for (IClientCallback client : activeClients.values()) {
            try {
                client.notify(message);
            } catch (RemoteException e) {
                // Client is likely disconnected, remove it to prevent future errors.
                // This part needs careful implementation to avoid ConcurrentModificationException,
                // but ConcurrentHashMap is safe to modify while iterating.
                System.err.println("Error notifying client, removing: " + e.getMessage());
            }
        }
    }

    // Method to notify a single, specific client by their username.
    public void notifyClient(String username, String message) {
        IClientCallback client = activeClients.get(username);
        if (client != null) {
            try {
                client.notify(message);
            } catch (RemoteException e) {
                System.err.println("Error notifying client " + username + ", removing: " + e.getMessage());
                // Client is likely disconnected, remove it to prevent future errors.
                activeClients.remove(username);
            }
        }
    }
} 