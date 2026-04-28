package edenmanager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PlantDAO.java
 * Data Access Object for the `plants` table.
 * Provides full CRUD operations plus search/filter functionality.
 */
public class PlantDAO {

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    /**
     * Inserts a new plant record (including BLOB image) into the database.
     *
     * @param plant Plant object to insert
     * @return true if insertion was successful
     */
    public boolean addPlant(Plant plant) {
        String sql = "INSERT INTO plants (name, species, category, description, image) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, plant.getName());
            ps.setString(2, plant.getSpecies());
            ps.setString(3, plant.getCategory());
            ps.setString(4, plant.getDescription());
            ps.setBytes(5, plant.getImage());   // Store image as BLOB
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PlantDAO] addPlant error: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // READ — All Plants (with images, for gallery)
    // -------------------------------------------------------------------------

    /**
     * Fetches all plant records from the database.
     *
     * @return List of all Plant objects
     */
    public List<Plant> getAllPlants() {
        List<Plant> list = new ArrayList<>();
        String sql = "SELECT id, name, species, category, description, image FROM plants ORDER BY id DESC";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PlantDAO] getAllPlants error: " + e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // READ — Single Plant by ID
    // -------------------------------------------------------------------------

    /**
     * Fetches a single plant by its primary key.
     *
     * @param id Plant ID
     * @return Plant object or null if not found
     */
    public Plant getPlantById(int id) {
        String sql = "SELECT id, name, species, category, description, image FROM plants WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[PlantDAO] getPlantById error: " + e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // SEARCH & FILTER
    // -------------------------------------------------------------------------

    /**
     * Searches plants by name or species (case-insensitive LIKE).
     *
     * @param keyword Search term
     * @return Matching Plant list
     */
    public List<Plant> searchPlants(String keyword) {
        List<Plant> list = new ArrayList<>();
        String sql = "SELECT id, name, species, category, description, image "
                   + "FROM plants WHERE name LIKE ? OR species LIKE ? ORDER BY name";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PlantDAO] searchPlants error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Filters plants by category.
     *
     * @param category Category string (e.g. "Flowering", "Succulent")
     * @return Filtered Plant list
     */
    public List<Plant> filterByCategory(String category) {
        List<Plant> list = new ArrayList<>();
        String sql = "SELECT id, name, species, category, description, image "
                   + "FROM plants WHERE category = ? ORDER BY name";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PlantDAO] filterByCategory error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all distinct categories from the plants table.
     *
     * @return List of category strings
     */
    public List<String> getAllCategories() {
        List<String> cats = new ArrayList<>();
        cats.add("All"); // default "show all" option
        String sql = "SELECT DISTINCT category FROM plants ORDER BY category";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) cats.add(rs.getString("category"));
        } catch (SQLException e) {
            System.err.println("[PlantDAO] getAllCategories error: " + e.getMessage());
        }
        return cats;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    /**
     * Updates an existing plant record.
     * If plant.getImage() is null, the existing image is preserved.
     *
     * @param plant Plant with updated data (must have valid ID)
     * @return true if update succeeded
     */
    public boolean updatePlant(Plant plant) {
        String sql;
        boolean updateImage = (plant.getImage() != null);

        if (updateImage) {
            sql = "UPDATE plants SET name=?, species=?, category=?, description=?, image=? WHERE id=?";
        } else {
            sql = "UPDATE plants SET name=?, species=?, category=?, description=? WHERE id=?";
        }

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, plant.getName());
            ps.setString(2, plant.getSpecies());
            ps.setString(3, plant.getCategory());
            ps.setString(4, plant.getDescription());
            if (updateImage) {
                ps.setBytes(5, plant.getImage());
                ps.setInt(6, plant.getId());
            } else {
                ps.setInt(5, plant.getId());
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PlantDAO] updatePlant error: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    /**
     * Deletes a plant record by ID.
     *
     * @param id Plant ID to delete
     * @return true if deletion succeeded
     */
    public boolean deletePlant(int id) {
        String sql = "DELETE FROM plants WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PlantDAO] deletePlant error: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // REPORT
    // -------------------------------------------------------------------------

    /**
     * Fetches all plants without images (lightweight, for reports/tables).
     *
     * @return List of Plant objects with null image field
     */
    public List<Plant> getAllPlantsNoImage() {
        List<Plant> list = new ArrayList<>();
        String sql = "SELECT id, name, species, category, description FROM plants ORDER BY name";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Plant p = new Plant();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setSpecies(rs.getString("species"));
                p.setCategory(rs.getString("category"));
                p.setDescription(rs.getString("description"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("[PlantDAO] getAllPlantsNoImage error: " + e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // PRIVATE HELPERS
    // -------------------------------------------------------------------------

    /** Maps a ResultSet row to a Plant object. */
    private Plant mapRow(ResultSet rs) throws SQLException {
        Plant p = new Plant();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setSpecies(rs.getString("species"));
        p.setCategory(rs.getString("category"));
        p.setDescription(rs.getString("description"));
        p.setImage(rs.getBytes("image"));
        return p;
    }
}