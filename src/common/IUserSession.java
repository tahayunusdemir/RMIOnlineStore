package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Defines the remote methods available to a logged-in customer.
 * This interface handles all customer-specific actions like browsing products,
 * managing the shopping cart, and placing orders.
 */
public interface IUserSession extends Remote {
    /**
     * Retrieves a list of all available products.
     * @return A list of Product objects.
     * @throws RemoteException if a communication-related error occurs.
     */
    List<Product> browseProducts() throws RemoteException;

    /**
     * Adds a specified quantity of a product to the user's shopping cart.
     * @param productId The ID of the product to add.
     * @param quantity The number of units to add.
     * @throws RemoteException if the product doesn't exist, stock is insufficient, or another error occurs.
     */
    void addToCart(int productId, int quantity) throws RemoteException;

    /**
     * Retrieves the contents of the user's shopping cart.
     * @return A map where keys are Product objects and values are their quantities in the cart.
     * @throws RemoteException if a communication-related error occurs.
     */
    Map<Product, Integer> viewCart() throws RemoteException;

    /**
     * Removes a product entirely from the user's shopping cart.
     * @param productId The ID of the product to remove.
     * @throws RemoteException if a communication-related error occurs.
     */
    void removeFromCart(int productId) throws RemoteException;

    /**
     * Clears all items from the user's shopping cart.
     * @throws RemoteException if a communication-related error occurs.
     */
    void clearCart() throws RemoteException;

    /**
     * Places an order with the items currently in the shopping cart.
     * This action will clear the cart upon success.
     * @return An Order object representing the newly created order.
     * @throws RemoteException if the cart is empty or a database error occurs during the transaction.
     */
    Order placeOrder() throws RemoteException;

    /**
     * Retrieves the order history for the current customer.
     * @return A list of Order objects.
     * @throws RemoteException if a communication-related error occurs.
     */
    List<Order> getOrderHistory() throws RemoteException;

    /**
     * Logs the current user out of their session.
     * @throws RemoteException if a communication-related error occurs.
     */
    void logout() throws RemoteException;
} 