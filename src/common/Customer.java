package common;

import java.io.Serializable;

/**
 * Represents a customer account.
 * This Data Transfer Object (DTO) holds all information related to a customer.
 * It is marked as Serializable to be sent between the RMI server and client.
 */
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id; // Unique identifier for the customer.
    private String username; // The customer's login username.
    private String password; // The customer's password (should be hashed in a real application).
    private String name; // The customer's full name.
    private String address; // The customer's shipping address.

    public Customer(int id, String username, String password, String name, String address) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
} 