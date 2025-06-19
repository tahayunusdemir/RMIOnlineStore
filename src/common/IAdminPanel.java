package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Defines the remote methods available to a logged-in administrator.
 * This interface provides functionalities for managing the store's inventory,
 * orders, and system-wide data.
 */
public interface IAdminPanel extends Remote {
    /**
     * Adds a new product to the store's catalog.
     * @param product The Product object to add.
     * @throws RemoteException if a database error occurs.
     */
    void addProduct(Product product) throws RemoteException;

    /**
     * Updates the stock quantity for a specific product.
     * @param productId The ID of the product to update.
     * @param newQuantity The new stock quantity.
     * @throws RemoteException if the product doesn't exist or a database error occurs.
     */
    void updateStock(int productId, int newQuantity) throws RemoteException;

    /**
     * Retrieves a summary of store statistics.
     * @return A string containing key metrics like total users, products, and revenue.
     * @throws RemoteException if a database error occurs.
     */
    String getStatistics() throws RemoteException;

    /**
     * Retrieves a list of all products in the store.
     * @return A list of all Product objects.
     * @throws RemoteException if a database error occurs.
     */
    List<Product> browseProducts() throws RemoteException;

    /**
     * Updates the status of an existing order.
     * @param orderId The ID of the order to update.
     * @param newStatus The new status for the order.
     * @throws RemoteException if the order doesn't exist or a database error occurs.
     */
    void updateOrderStatus(int orderId, Order.Status newStatus) throws RemoteException;

    /**
     * Retrieves a list of all orders placed by all customers.
     * @return A list of all Order objects.
     * @throws RemoteException if a database error occurs.
     */
    List<Order> viewAllOrders() throws RemoteException;

    /**
     * Adds a new product category.
     * @param categoryName The name of the new category.
     * @throws RemoteException if the category already exists or a database error occurs.
     */
    void addCategory(String categoryName) throws RemoteException;

    /**
     * Retrieves all product categories.
     * @return A list of all Category objects.
     * @throws RemoteException if a database error occurs.
     */
    List<Category> getAllCategories() throws RemoteException;

    /**
     * Deletes a product from the store.
     * @param productId The ID of the product to delete.
     * @throws RemoteException if the product is part of an existing order or a database error occurs.
     */
    void deleteProduct(int productId) throws RemoteException;

    /**
     * Deletes a product category.
     * @param categoryId The ID of the category to delete.
     * @throws RemoteException if the category is still assigned to products or a database error occurs.
     */
    void deleteCategory(int categoryId) throws RemoteException;
} 