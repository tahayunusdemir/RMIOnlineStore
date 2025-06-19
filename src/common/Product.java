package common;

import java.io.Serializable;

/**
 * Represents a product available in the store.
 * This Data Transfer Object (DTO) contains all attributes of a product
 * and is Serializable to be passed between the server and client.
 */
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id; // The unique identifier for the product.
    private String name; // The name of the product.
    private String description; // A detailed description of the product.
    private double price; // The price of a single unit of the product.
    private int stockQuantity; // The current number of units in stock.
    private String category; // The name of the category this product belongs to.
    private String brand; // The brand of the product (e.g., "Nike").
    private String size; // The size of the product (e.g., "42", "L").
    private String color; // The color of the product.

    public Product(int id, String name, String description, double price, int stockQuantity, String category, String brand, String size, String color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.brand = brand;
        this.size = size;
        this.color = color;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", size='" + size + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
} 