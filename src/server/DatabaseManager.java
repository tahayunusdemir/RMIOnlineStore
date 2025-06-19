package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the connection to the MySQL database.
 * This class provides a static method to get a new database connection.
 * It centralizes the database connection logic, including the JDBC URL and credentials.
 */
public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/rmi_onlinestore?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // <-- Enter your MySQL username here
    private static final String PASS = "6055"; // <-- Enter your MySQL password here

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