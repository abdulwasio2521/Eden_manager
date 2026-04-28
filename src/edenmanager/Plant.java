package edenmanager;

/**
 * Plant.java
 * Model / POJO class representing a plant record from the database.
 * Encapsulates all fields from the `plants` table.
 */
public class Plant {

    private int    id;
    private String name;
    private String species;
    private String category;
    private String description;
    private byte[] image;        // Raw BLOB bytes from MySQL

    // --- Constructors ---

    public Plant() {}

    public Plant(int id, String name, String species, String category,
                 String description, byte[] image) {
        this.id          = id;
        this.name        = name;
        this.species     = species;
        this.category    = category;
        this.description = description;
        this.image       = image;
    }

    // --- Getters ---

    public int    getId()          { return id; }
    public String getName()        { return name; }
    public String getSpecies()     { return species; }
    public String getCategory()    { return category; }
    public String getDescription() { return description; }
    public byte[] getImage()       { return image; }

    // --- Setters ---

    public void setId(int id)                   { this.id = id; }
    public void setName(String name)            { this.name = name; }
    public void setSpecies(String species)      { this.species = species; }
    public void setCategory(String category)    { this.category = category; }
    public void setDescription(String desc)     { this.description = desc; }
    public void setImage(byte[] image)          { this.image = image; }

    @Override
    public String toString() {
        return "Plant{id=" + id + ", name='" + name + "', species='" + species
                + "', category='" + category + "'}";
    }
}