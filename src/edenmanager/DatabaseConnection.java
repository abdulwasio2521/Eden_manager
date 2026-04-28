package edenmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection.java
 * Singleton class to manage a single JDBC connection to the eden_db MySQL database.
 * Uses XAMPP default settings: host=localhost, port=3306, user=root, password="".
 */
public class DatabaseConnection {

    // --- Connection credentials ---
    private static final String URL      = "jdbc:mysql://localhost:3306/eden_db?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    // Singleton instance holder
    private static Connection connection = null;

    // Private constructor — no instantiation allowed
    private DatabaseConnection() {}

    /**
     * Returns the single shared Connection.
     * Creates it on first call; reuses it on subsequent calls.
     *
     * @return active java.sql.Connection
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connection established successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] MySQL Driver not found. Add mysql-connector-j to classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Closes the active connection (call on application exit).
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}