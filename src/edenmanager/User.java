package edenmanager;

/**
 * User.java
 * Model / POJO class representing a user record from the `users` table.
 */
public class User {

    private int    id;
    private String fullname;
    private String username;
    private String password;
    private String role;     // "Admin" or "User"

    // --- Constructors ---

    public User() {}

    public User(int id, String fullname, String username, String password, String role) {
        this.id       = id;
        this.fullname = fullname;
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    // --- Getters ---

    public int    getId()       { return id; }
    public String getFullname() { return fullname; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole()     { return role; }

    // --- Setters ---

    public void setId(int id)             { this.id = id; }
    public void setFullname(String fn)    { this.fullname = fn; }
    public void setUsername(String un)    { this.username = un; }
    public void setPassword(String pw)    { this.password = pw; }
    public void setRole(String role)      { this.role = role; }

    /** Convenience: checks if user has Admin privileges. */
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}