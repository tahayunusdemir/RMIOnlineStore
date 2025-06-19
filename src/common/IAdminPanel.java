package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAdminPanel extends Remote {
    void addProduct(Product product) throws RemoteException;
    void updateStock(int productId, int newQuantity) throws RemoteException;
    String getStatistics() throws RemoteException;
} 