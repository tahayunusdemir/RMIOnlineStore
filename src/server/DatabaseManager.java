package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/rmi_onlinestore?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // <-- Kendi MySQL kullanıcı adınızı girin
    private static final String PASS = "6055"; // <-- Kendi MySQL şifrenizi girin

    private DatabaseManager() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
} 