package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IAdminPanel extends Remote {
    void addProduct(Product product) throws RemoteException;
    void updateStock(int productId, int newQuantity) throws RemoteException;
    String getStatistics() throws RemoteException;
    List<Product> browseProducts() throws RemoteException;
    void updateOrderStatus(int orderId, Order.Status newStatus) throws RemoteException;
    List<Order> viewAllOrders() throws RemoteException;
    void addCategory(String categoryName) throws RemoteException;
    List<Category> getAllCategories() throws RemoteException;
    void deleteProduct(int productId) throws RemoteException;
    void deleteCategory(int categoryId) throws RemoteException;
} 