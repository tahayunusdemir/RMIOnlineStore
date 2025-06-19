package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClientCallback extends Remote {
    void notify(String message) throws RemoteException;
} 