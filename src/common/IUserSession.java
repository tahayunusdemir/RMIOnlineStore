package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface IUserSession extends Remote {
    List<Product> browseProducts() throws RemoteException;
    void addToCart(int productId, int quantity) throws RemoteException;
    Map<Product, Integer> viewCart() throws RemoteException;
    Order placeOrder() throws RemoteException;
    List<Order> getOrderHistory() throws RemoteException;
    void logout() throws RemoteException;
} 