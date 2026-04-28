package edenmanager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO.java
 * Data Access Object for the `users` table.
 * Handles login validation, signup, and user listing.
 */
public class UserDAO {

    /** Secret admin registration key */
    public static final String ADMIN_SECRET_KEY = "EDEN123";

    // -------------------------------------------------------------------------
    // AUTHENTICATION
    // -------------------------------------------------------------------------

    /**
     * Validates login credentials against the database.
     *
     * @param username Entered username
     * @param password Entered password
     * @return Matching User object, or null if credentials are wrong
     */
    public User login(String username, String password) {
        String sql = "SELECT id, fullname, username, password, role FROM users "
                   + "WHERE username = ? AND password = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("fullname"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] login error: " + e.getMessage());
        }
        return null; // Login failed
    }

    // -------------------------------------------------------------------------
    // SIGNUP / REGISTRATION
    // -------------------------------------------------------------------------

    /**
     * Registers a new user.
     * Role is determined by whether the secretKey matches ADMIN_SECRET_KEY.
     *
     * @param fullname   Full display name
     * @param username   Unique username
     * @param password   Plain-text password
     * @param secretKey  Optional admin key ("EDEN123" = Admin, anything else = User)
     * @return true if registration succeeded
     */
    public boolean signup(String fullname, String username, String password, String secretKey) {
        // Determine role based on secret key
        String role = ADMIN_SECRET_KEY.equals(secretKey) ? "Admin" : "User";

        String sql = "INSERT INTO users (fullname, username, password, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, fullname);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, role);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            // Username already exists (UNIQUE constraint)
            System.err.println("[UserDAO] Username already taken: " + username);
            return false;
        } catch (SQLException e) {
            System.err.println("[UserDAO] signup error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a username is already taken.
     *
     * @param username Username to check
     * @return true if the username already exists
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] usernameExists error: " + e.getMessage());
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // READ (Admin use — user management)
    // -------------------------------------------------------------------------

    /**
     * Returns all registered users (for admin reports/management).
     *
     * @return List of all User objects
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, fullname, username, password, role FROM users ORDER BY id";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("fullname"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllUsers error: " + e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    /**
     * Deletes a user by ID (Admin only).
     *
     * @param userId User ID to remove
     * @return true if deletion succeeded
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] deleteUser error: " + e.getMessage());
            return false;
        }
    }
}