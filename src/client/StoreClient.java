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
        System.out.print("Enter new username (e.g., taha.demir): ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter your full name (e.g., Taha Demir): ");
        String name = scanner.nextLine();
        System.out.print("Enter your address (e.g., 123 Main St, Anytown): ");
        String address = scanner.nextLine();
        Customer newCustomer = new Customer(0, username, password, name, address); // ID will be set by DB
        factory.registerCustomer(newCustomer);
        System.out.println("Registration successful! You can now log in.");
    }

    private static void loginAdmin(Scanner scanner) throws RemoteException {
        System.out.print("Enter admin username (default: admin): ");
        String username = scanner.nextLine();
        System.out.print("Enter admin password (default: admin): ");
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
                        System.out.print("Enter Product ID to add (e.g., 1): ");
                        int prodId = scanner.nextInt();
                        System.out.print("Enter quantity (e.g., 1): ");
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
            System.out.println("1. Browse Products");
            System.out.println("2. Add Product");
            System.out.println("3. Update Stock");
            System.out.println("4. Delete Product");
            System.out.println("5. View Statistics");
            System.out.println("6. List Orders");
            System.out.println("7. Update Order Status");
            System.out.println("8. List Categories");
            System.out.println("9. Add New Category");
            System.out.println("10. Delete Category");
            System.out.println("11. Logout");
            System.out.print("Choose an option: ");
            int choice = getIntInput(scanner);
            scanner.nextLine(); // Consume newline

            try {
                switch (choice) {
                    case 1:
                        browseProductsAdmin();
                        break;
                    case 2:
                        addProductFlow(scanner);
                        break;
                    case 3:
                        updateStockFlow(scanner);
                        break;
                    case 4:
                        deleteProductFlow(scanner);
                        break;
                    case 5:
                        viewStatistics();
                        break;
                    case 6:
                        listOrdersAdmin();
                        break;
                    case 7:
                        updateOrderStatusFlow(scanner);
                        break;
                    case 8:
                        listCategoriesAdmin();
                        break;
                    case 9:
                        addCategoryFlow(scanner);
                        break;
                    case 10:
                        deleteCategoryFlow(scanner);
                        break;
                    case 11:
                        adminPanel = null;
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

    // --- Helper for cancellable input ---
    private static String getStringInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        if ("cancel".equalsIgnoreCase(input)) {
            return null;
        }
        return input;
    }
    
    private static int getIntInput(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next(); // discard non-int input
            System.out.print("Choose an option: ");
        }
        return scanner.nextInt();
    }

    private static double getDoubleInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            String input = scanner.next();
            if ("cancel".equalsIgnoreCase(input)) return -1;
            System.out.println("Invalid input. Please enter a valid number.");
            System.out.print(prompt);
        }
        double value = scanner.nextDouble();
        scanner.nextLine(); // consume newline
        return value;
    }

    // --- Admin Action Flows ---

    private static void browseProductsAdmin() throws RemoteException {
        List<Product> products = adminPanel.browseProducts();
        System.out.println("--- All Products ---");
        products.forEach(p -> System.out.printf("ID: %d, Name: %s, Price: %.2f, Stock: %d, Category: %s%n", p.getId(), p.getName(), p.getPrice(), p.getStockQuantity(), p.getCategory()));
    }

    private static void addProductFlow(Scanner scanner) throws RemoteException {
        System.out.println("\n--- Add New Product (type 'cancel' at any prompt to exit) ---");
        String name = getStringInput(scanner, "Product Name (e.g., Asics Gel-Kayano 30): ");
        if (name == null) { System.out.println("Cancelled."); return; }

        String desc = getStringInput(scanner, "Description (e.g., Stability running shoe): ");
        if (desc == null) { System.out.println("Cancelled."); return; }
        
        System.out.print("Price (e.g., 160.00): ");
        double price = getDoubleInput(scanner, "");
        if (price < 0) { System.out.println("Cancelled."); return; }
        
        System.out.print("Stock Quantity (e.g., 75): ");
        String stockStr = getStringInput(scanner, "");
        if (stockStr == null) { System.out.println("Cancelled."); return; }
        int stock = Integer.parseInt(stockStr);

        List<Category> categories = adminPanel.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println("No categories found. Please add a category first.");
            return;
        }
        System.out.println("Select a category:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, categories.get(i).getName());
        }
        System.out.print("Choose category number: ");
        String catChoiceStr = getStringInput(scanner, "");
        if (catChoiceStr == null) { System.out.println("Cancelled."); return; }
        int categoryChoice = Integer.parseInt(catChoiceStr);

        if (categoryChoice <= 0 || categoryChoice > categories.size()) {
            System.out.println("Invalid category choice.");
            return;
        }
        String cat = categories.get(categoryChoice - 1).getName();

        String brand = getStringInput(scanner, "Brand (e.g., Asics): ");
        if (brand == null) { System.out.println("Cancelled."); return; }

        String size = getStringInput(scanner, "Size (e.g., 44): ");
        if (size == null) { System.out.println("Cancelled."); return; }

        String color = getStringInput(scanner, "Color (e.g., Blue/Black): ");
        if (color == null) { System.out.println("Cancelled."); return; }

        Product newProduct = new Product(0, name, desc, price, stock, cat, brand, size, color);
        adminPanel.addProduct(newProduct);
        System.out.println("Product added.");
    }

    private static void updateStockFlow(Scanner scanner) throws RemoteException {
        System.out.println("\n--- Update Stock (type 'cancel' to exit) ---");
        browseProductsAdmin();
        String prodIdStr = getStringInput(scanner, "Enter Product ID to update: ");
        if (prodIdStr == null) { System.out.println("Cancelled."); return; }
        int prodId = Integer.parseInt(prodIdStr);

        String newQtyStr = getStringInput(scanner, "Enter new stock quantity: ");
        if (newQtyStr == null) { System.out.println("Cancelled."); return; }
        int newQty = Integer.parseInt(newQtyStr);
        
        adminPanel.updateStock(prodId, newQty);
        System.out.println("Stock updated.");
    }
    
    private static void deleteProductFlow(Scanner scanner) throws RemoteException {
        System.out.println("\n--- Delete Product (type 'cancel' to exit) ---");
        browseProductsAdmin();
        String prodIdStr = getStringInput(scanner, "Enter Product ID to delete: ");
        if (prodIdStr == null) { System.out.println("Cancelled."); return; }
        int prodId = Integer.parseInt(prodIdStr);
        
        adminPanel.deleteProduct(prodId);
        System.out.println("Request to delete product sent.");
    }

    private static void viewStatistics() throws RemoteException {
        String stats = adminPanel.getStatistics();
        System.out.println("\n--- Server Statistics ---");
        System.out.println(stats);
    }

    private static void listOrdersAdmin() throws RemoteException {
        List<Order> allOrders = adminPanel.viewAllOrders();
        System.out.println("\n--- All Orders ---");
        if (allOrders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        allOrders.forEach(o -> System.out.printf("ID: %d, CustomerID: %d, Date: %s, Total: %.2f, Status: %s%n", o.getId(), o.getCustomerId(), o.getOrderDate(), o.getTotalAmount(), o.getStatus()));
    }
    
    private static void updateOrderStatusFlow(Scanner scanner) throws RemoteException {
        System.out.println("\n--- Update Order Status (type 'cancel' to exit) ---");
        listOrdersAdmin();
        String orderIdStr = getStringInput(scanner, "\nEnter Order ID to update: ");
        if (orderIdStr == null) { System.out.println("Cancelled."); return; }
        int orderId = Integer.parseInt(orderIdStr);

        System.out.println("Select new status:");
        int i = 1;
        for (Order.Status s : Order.Status.values()) {
            System.out.printf("%d. %s%n", i++, s.name());
        }
        
        String statusChoiceStr = getStringInput(scanner, "Choose status number: ");
        if (statusChoiceStr == null) { System.out.println("Cancelled."); return; }
        int statusChoice = Integer.parseInt(statusChoiceStr);

        if (statusChoice > 0 && statusChoice <= Order.Status.values().length) {
            Order.Status newStatus = Order.Status.values()[statusChoice - 1];
            adminPanel.updateOrderStatus(orderId, newStatus);
            System.out.println("Order status update request sent.");
        } else {
            System.out.println("Invalid status choice.");
        }
    }
    
    private static void listCategoriesAdmin() throws RemoteException {
        List<Category> currentCategories = adminPanel.getAllCategories();
        System.out.println("\n--- All Categories ---");
        if (currentCategories.isEmpty()) {
            System.out.println("No categories exist yet.");
        } else {
            currentCategories.forEach(c -> System.out.printf("ID: %d, Name: %s%n", c.getId(), c.getName()));
        }
    }
    
    private static void addCategoryFlow(Scanner scanner) throws RemoteException {
        System.out.println("\n--- Add New Category (type 'cancel' to exit) ---");
        listCategoriesAdmin();
        String newCatName = getStringInput(scanner, "\nEnter new category name: ");
        if (newCatName == null) { System.out.println("Cancelled."); return; }

        if (newCatName.trim().isEmpty()) {
            System.out.println("Category name cannot be empty.");
        } else {
            adminPanel.addCategory(newCatName);
            System.out.println("Request to add category '" + newCatName + "' sent.");
        }
    }
    
    private static void deleteCategoryFlow(Scanner scanner) throws RemoteException {
        System.out.println("\n--- Delete Category (type 'cancel' to exit) ---");
        listCategoriesAdmin();
        String catIdStr = getStringInput(scanner, "\nEnter Category ID to delete: ");
        if (catIdStr == null) { System.out.println("Cancelled."); return; }
        int catId = Integer.parseInt(catIdStr);

        adminPanel.deleteCategory(catId);
        System.out.println("Request to delete category sent.");
    }
}
