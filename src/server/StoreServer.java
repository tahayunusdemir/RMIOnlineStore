package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class StoreServer {
    public static void main(String[] args) {
        try {
            // Start the RMI registry on the default port 1099
            LocateRegistry.createRegistry(1099);
            System.out.println("RMI registry started.");

            // Create a single instance of the factory implementation.
            // This object will handle all incoming requests for sessions.
            StoreFactoryImpl factory = new StoreFactoryImpl();
            System.out.println("StoreFactory implementation created.");

            // Bind the remote factory object to the RMI registry with the name "StoreFactory".
            // Clients will use this name to look up the factory.
            Naming.rebind("rmi://localhost/StoreFactory", factory);
            System.out.println("StoreFactory bound in registry.");

            System.out.println("Server is ready.");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
} 