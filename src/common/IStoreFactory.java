package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IStoreFactory extends Remote {
    IUserSession login(String username, String password, IClientCallback clientCallback) throws RemoteException;
    void registerCustomer(Customer newCustomer) throws RemoteException;
    IAdminPanel adminLogin(String username, String password) throws RemoteException;
} 