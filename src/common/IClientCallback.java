package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Defines the callback interface that the server uses to send asynchronous messages to the client.
 */
public interface IClientCallback extends Remote {
    /**
     * Called by the server to send a notification message to the client.
     * @param message The message from the server.
     * @throws RemoteException if a communication-related error occurs.
     */
    void notify(String message) throws RemoteException;
} 