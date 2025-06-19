package client;

import common.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class StoreClient extends UnicastRemoteObject implements IClientCallback {

    private static IStoreFactory factory;
    private static IUserSession userSession;
    private static IAdminPanel adminPanel;

    public StoreClient() throws RemoteException {
        super();
    }

    @Override
    public void notify(String message) throws RemoteException {
        System.out.println("\n[SERVER NOTIFICATION] -> " + message);
    }

    public static void main(String[] args) {
        try {
            StoreClient client = new StoreClient();
            factory = (IStoreFactory) Naming.lookup("rmi://localhost/StoreFactory");
            System.out.println("Connected to the store server!");
            handleMainMenu(client);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleMainMenu(StoreClient client) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Admin Login");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            try {
                switch (choice) {
                    case 1:
                        loginUser(scanner, client);
                        if (userSession != null) {
                            handleUserSession(scanner);
                        }
                        break;
                    case 2:
                        registerUser(scanner);
                        break;
                    case 3:
                        loginAdmin(scanner);
                        if (adminPanel != null) {
                            handleAdminPanel(scanner);
                        }
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        System.exit(0);
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (RemoteException e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private static void loginUser(Scanner scanner, StoreClient client) throws RemoteException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        userSession = factory.login(username, password, client);
        if (userSession == null) {
            System.out.println("Login failed. Please check your credentials.");
        } else {
            System.out.println("Login successful!");
        }
    }
    
    private static void registerUser(Scanner scanner) throws RemoteException {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter your full name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your address: ");
        String address = scanner.nextLine();
        Customer newCustomer = new Customer(0, username, password, name, address); // ID will be set by DB
        factory.registerCustomer(newCustomer);
        System.out.println("Registration successful! You can now log in.");
    }

    private static void loginAdmin(Scanner scanner) throws RemoteException {
        System.out.print("Enter admin username: ");
        String username = scanner.nextLine();
        System.out.print("Enter admin password: ");
        String password = scanner.nextLine();
        adminPanel = factory.adminLogin(username, password);
        if (adminPanel == null) {
            System.out.println("Admin login failed.");
        } else {
            System.out.println("Admin login successful!");
        }
    }

    private static void handleUserSession(Scanner scanner) {
        while (true) {
            System.out.println("\n--- User Menu ---");
            System.out.println("1. Browse Products");
            System.out.println("2. Add to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Place Order");
            System.out.println("5. View Order History");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            try {
                switch (choice) {
                    case 1:
                        List<Product> products = userSession.browseProducts();
                        System.out.println("--- Available Products ---");
                        products.forEach(p -> System.out.printf("ID: %d, Name: %s, Price: %.2f, Stock: %d%n", p.getId(), p.getName(), p.getPrice(), p.getStockQuantity()));
                        break;
                    case 2:
                        System.out.print("Enter Product ID to add: ");
                        int prodId = scanner.nextInt();
                        System.out.print("Enter quantity: ");
                        int qty = scanner.nextInt();
                        scanner.nextLine();
                        userSession.addToCart(prodId, qty);
                        System.out.println("Product added to cart.");
                        break;
                    case 3:
                        Map<Product, Integer> cart = userSession.viewCart();
                        System.out.println("--- Your Cart ---");
                        if (cart.isEmpty()) {
                            System.out.println("Your cart is empty.");
                        } else {
                            cart.forEach((p, q) -> System.out.printf("Product: %s, Quantity: %d%n", p.getName(), q));
                        }
                        break;
                    case 4:
                        Order order = userSession.placeOrder();
                        System.out.println("Order placed successfully! Order ID: " + order.getId());
                        break;
                    case 5:
                        List<Order> history = userSession.getOrderHistory();
                        System.out.println("--- Your Order History ---");
                        history.forEach(o -> System.out.printf("Order ID: %d, Date: %s, Total: %.2f, Status: %s%n", o.getId(), o.getOrderDate(), o.getTotalAmount(), o.getStatus()));
                        break;
                    case 6:
                        userSession.logout();
                        userSession = null;
                        System.out.println("Logged out.");
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (RemoteException e) {
                System.err.println("An error occurred: " + e.getMessage());
                // On critical error, we might want to logout
                if(e.getCause() != null) System.err.println("Cause: " + e.getCause().getMessage());
            }
        }
    }

    private static void handleAdminPanel(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Admin Panel ---");
            System.out.println("1. Add Product");
            System.out.println("2. Update Stock");
            System.out.println("3. View Statistics");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            try {
                switch (choice) {
                    case 1:
                        System.out.print("Product Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Description: ");
                        String desc = scanner.nextLine();
                        System.out.print("Price: ");
                        double price = scanner.nextDouble();
                        System.out.print("Stock Quantity: ");
                        int stock = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Category: ");
                        String cat = scanner.nextLine();
                        System.out.print("Brand: ");
                        String brand = scanner.nextLine();
                        System.out.print("Size: ");
                        String size = scanner.nextLine();
                        System.out.print("Color: ");
                        String color = scanner.nextLine();
                        Product newProduct = new Product(0, name, desc, price, stock, cat, brand, size, color);
                        adminPanel.addProduct(newProduct);
                        System.out.println("Product added.");
                        break;
                    case 2:
                        System.out.print("Enter Product ID to update: ");
                        int prodId = scanner.nextInt();
                        System.out.print("Enter new stock quantity: ");
                        int newQty = scanner.nextInt();
                        scanner.nextLine();
                        adminPanel.updateStock(prodId, newQty);
                        System.out.println("Stock updated.");
                        break;
                    case 3:
                        String stats = adminPanel.getStatistics();
                        System.out.println("--- Server Statistics ---");
                        System.out.println(stats);
                        break;
                    case 4:
                        adminPanel = null; // No remote logout method for admin
                        System.out.println("Admin logged out.");
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (RemoteException e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
        }
    }
}
