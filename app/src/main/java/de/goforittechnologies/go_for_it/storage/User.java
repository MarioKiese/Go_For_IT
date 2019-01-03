package de.goforittechnologies.go_for_it.storage;

/**
 * @author  Mario Kiese and Tom Hammerbacher
 * @version 0.8.
 *
 * class to represent and temporary store user-data
 * contain getter ad setter methods
 */

public class User {

    private String id;
    private String name;
    private String image;

    public User() {
    }

    /**
     * Constructor to create a new user
     *
     * @param id users unique id
     * @param name user-name
     * @param image image-tag
     */
    public User(String id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
