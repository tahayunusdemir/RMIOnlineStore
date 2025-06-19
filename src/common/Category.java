package common;

import java.io.Serializable;

/**
 * Represents a product category.
 * This is a simple Data Transfer Object (DTO) used to pass category information
 * between the server and the client.
 */
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id; // The unique identifier for the category.
    private String name; // The name of the category (e.g., "Running Shoes").

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
} 