package common;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Represents a customer order.
 * This DTO contains all details of an order, including the customer, items,
 * and its current status. It is Serializable for RMI transport.
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Represents the possible states of an order.
     */
    public enum Status {
        PENDING, // Order has been placed but not yet processed.
        PROCESSING, // Order is being prepared for shipment.
        SHIPPED, // Order has been shipped to the customer.
        DELIVERED, // Order has been successfully delivered.
        CANCELLED // Order has been cancelled.
    }

    private int id; // The unique identifier for the order.
    private int customerId; // The ID of the customer who placed the order.
    private Date orderDate; // The date and time the order was placed.
    private List<OrderItem> items; // The list of items included in this order.
    private double totalAmount; // The total cost of the order.
    private Status status; // The current status of the order (e.g., PENDING).

    public Order(int id, int customerId, Date orderDate, List<OrderItem> items, double totalAmount, Status status) {
        this.id = id;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", orderDate=" + orderDate +
                ", items=" + items +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                '}';
    }
} 