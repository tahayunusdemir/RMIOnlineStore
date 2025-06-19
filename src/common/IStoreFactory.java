package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The main factory interface for the store.
 * This acts as the entry point for clients to interact with the server,
 * allowing them to log in, register, or gain admin access.
 */
public interface IStoreFactory extends Remote {
    /**
     * Attempts to log in a customer.
     * @param username The customer's username.
     * @param password The customer's password.
     * @param clientCallback A reference to the client's callback object for server-to-client notifications.
     * @return An IUserSession object if login is successful, otherwise null.
     * @throws RemoteException if a communication-related error occurs.
     */
    IUserSession login(String username, String password, IClientCallback clientCallback) throws RemoteException;

    /**
     * Registers a new customer in the system.
     * @param newCustomer The Customer object containing the new user's details.
     * @throws RemoteException if the username already exists or a database error occurs.
     */
    void registerCustomer(Customer newCustomer) throws RemoteException;

    /**
     * Attempts to log in an administrator.
     * @param username The admin's username.
     * @param password The admin's password.
     * @return An IAdminPanel object if login is successful, otherwise null.
     * @throws RemoteException if a communication-related error occurs.
     */
    IAdminPanel adminLogin(String username, String password) throws RemoteException;

    /**
     * Logs out a user by removing their callback reference from the server.
     * @param username The username of the client to log out.
     * @throws RemoteException if a communication-related error occurs.
     */
    void logout(String username) throws RemoteException;
} 