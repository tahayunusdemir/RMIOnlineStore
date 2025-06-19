package common;

import java.io.Serializable;

/**
 * Represents a single line item within an Order.
 * This DTO links a Product to an Order and stores the quantity and price at the time of purchase.
 * It is Serializable for RMI transport.
 */
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int productId; // The ID of the product in this line item.
    private int quantity; // The number of units of the product purchased.
    private double price; // The price of a single unit at the time of purchase.

    public OrderItem(int productId, int quantity, double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
} 