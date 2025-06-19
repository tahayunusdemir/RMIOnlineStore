package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class StoreServer {
    public static void main(String[] args) {
        try {
            // Start the RMI registry on port 1099
            LocateRegistry.createRegistry(1099);
            System.out.println("RMI registry started.");

            // Create an instance of the factory implementation
            StoreFactoryImpl factory = new StoreFactoryImpl();
            System.out.println("StoreFactory implementation created.");

            // Bind the factory instance to the name "StoreFactory"
            Naming.rebind("rmi://localhost/StoreFactory", factory);
            System.out.println("StoreFactory bound in registry.");

            System.out.println("Server is ready.");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
} 