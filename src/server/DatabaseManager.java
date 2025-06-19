package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/rmi_onlinestore?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // <-- Kendi MySQL kullanıcı adınızı girin
    private static final String PASS = "6055"; // <-- Kendi MySQL şifrenizi girin

    private static Connection connection = null;

    private DatabaseManager() {
        // Singleton pattern
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Sürücüyü yükle
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Bağlantıyı oluştur
                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Database connection established successfully.");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("Failed to connect to the database.");
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
} 