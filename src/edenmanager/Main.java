package edenmanager;

import javax.swing.SwingUtilities;

/**
 * Main.java
 * Application entry point for Eden Manager.
 *
 * Startup sequence:
 *   1. Apply Nimbus Look & Feel (flat UI theme)
 *   2. Verify DB connection on startup
 *   3. Launch LoginFrame on the Event Dispatch Thread (EDT)
 *
 * Run Requirements:
 *   - XAMPP MySQL running on port 3306
 *   - eden_db database created (run eden_db.sql first)
 *   - mysql-connector-j-*.jar in the project classpath
 */
public class Main {

    public static void main(String[] args) {

        // 1. Apply flat look and feel before any UI is created
        UITheme.applyLookAndFeel();

        // 2. Test DB connection at startup
        if (DatabaseConnection.getConnection() == null) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Could not connect to the database.\n\n" +
                "Please ensure:\n" +
                "  • XAMPP MySQL is running\n" +
                "  • Database 'eden_db' exists (run eden_db.sql)\n" +
                "  • mysql-connector-j is in the classpath",
                "Database Connection Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // 3. Launch the Login window on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });

        // 4. Register shutdown hook to close DB connection cleanly
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnection.closeConnection();
            System.out.println("[Eden Manager] Application closed.");
        }));
    }
}