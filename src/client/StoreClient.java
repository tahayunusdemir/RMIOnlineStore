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

    /**
     * This method is called by the server to deliver an asynchronous notification.
     */
    @Override
    public void notify(String message) throws RemoteException {
        System.out.println("\n[SERVER NOTIFICATION] -> " + message);
    }

    public static void main(String[] args) {
        try {
            // The client must also be a remote object to receive callbacks.
            StoreClient client = new StoreClient();
            // Look up the remote factory object from the RMI registry.
            factory = (IStoreFactory) Naming.lookup("rmi://localhost/StoreFactory");
            System.out.println("Connected to the store server!");
            // Start the main user interaction loop.
            handleMainMenu(client);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the main menu logic, directing the user to login, register, or exit.
     */
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

    /**
     * Guides the user through the login process.
     */
    private static void loginUser(Scanner scanner, StoreClient client) throws RemoteException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        // The 'client' instance is passed as the callback object.
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

    /**
     * Guides the administrator through the login process.
     */
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

    /**
     * Handles the menu and actions for a logged-in customer.
     */
    private static void handleUserSession(Scanner scanner) {
        while (true) {
            System.out.println("\n--- User Menu ---");
            System.out.println("1. Browse Products");
            System.out.println("2. Add to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Remove from Cart");
            System.out.println("5. Clear Cart");
            System.out.println("6. Place Order");
            System.out.println("7. View Order History");
            System.out.println("8. Logout");
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
                        viewCartDetailed();
                        break;
                    case 4:
                        removeFromCartFlow(scanner);
                        break;
                    case 5:
                        System.out.print("Are you sure you want to clear your entire cart? (y/n): ");
                        String confirmation = scanner.nextLine();
                        if ("y".equalsIgnoreCase(confirmation)) {
                            userSession.clearCart();
                            System.out.println("Your cart has been cleared.");
                        } else {
                            System.out.println("Operation cancelled.");
                        }
                        break;
                    case 6:
                        Order order = userSession.placeOrder();
                        System.out.println("\n--- Order Confirmation ---");
                        System.out.println("Order placed successfully!");
                        System.out.println("Order ID: " + order.getId());
                        System.out.println("Order Date: " + order.getOrderDate());
                        System.out.printf("Total Amount: $%.2f%n", order.getTotalAmount());
                        System.out.println("Status: " + order.getStatus());
                        System.out.println("\nNote: Payment will be made upon delivery of the product.");
                        System.out.println("--- Thank You! ---");
                        break;
                    case 7:
                        List<Order> history = userSession.getOrderHistory();
                        System.out.println("--- Your Order History ---");
                        history.forEach(o -> System.out.printf("Order ID: %d, Date: %s, Total: %.2f, Status: %s%n", o.getId(), o.getOrderDate(), o.getTotalAmount(), o.getStatus()));
                        break;
                    case 8:
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

    private static void viewCartDetailed() throws RemoteException {
        Map<Product, Integer> cart = userSession.viewCart();
        System.out.println("--- Your Cart ---");
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
        } else {
            cart.forEach((p, q) -> System.out.printf("ID: %d, Product: %s, Quantity: %d%n", p.getId(), p.getName(), q));
        }
    }

    private static void removeFromCartFlow(Scanner scanner) throws RemoteException {
        System.out.println("\n--- Remove from Cart ---");
        viewCartDetailed();
        Map<Product, Integer> cart = userSession.viewCart();
        if (cart.isEmpty()) {
            return; // Nothing to remove
        }
        String prodIdStr = getStringInput(scanner, "Enter Product ID to remove (or 'cancel'): ");
        if (prodIdStr == null) {
            System.out.println("Cancelled.");
            return;
        }
        try {
            int prodId = Integer.parseInt(prodIdStr);
            userSession.removeFromCart(prodId);
            System.out.println("Product removed from cart.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid Product ID format.");
        }
    }

    /**
     * Handles the menu and actions for a logged-in administrator.
     */
    private static void handleAdminPanel(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Admin Panel ---");
            System.out.println("1. Browse Products");
            System.out.println("2. Add Product");
            System.out.println("3. Update Product");
            System.out.println("4. Update Stock");
            System.out.println("5. Delete Product");
            System.out.println("6. View Dashboard Statistics");
            System.out.println("7. View Advanced Reports");
            System.out.println("8. List Orders");
            System.out.println("9. Update Order Status");
            System.out.println("10. List Categories");
            System.out.println("11. Add New Category");
            System.out.println("12. Update Category");
            System.out.println("13. Delete Category");
            System.out.println("14. Logout");
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
                        editProductFlow(scanner);
                        break;
                    case 4:
                        updateStockFlow(scanner);
                        break;
                    case 5:
                        deleteProductFlow(scanner);
                        break;
                    case 6:
                        viewDashboardStatistics();
                        break;
                    case 7:
                        viewAdvancedReports();
                        break;
                    case 8:
                        listOrdersAdmin();
                        break;
                    case 9:
                        updateOrderStatusFlow(scanner);
                        break;
                    case 10:
                        listCategoriesAdmin();
                        break;
                    case 11:
                        addCategoryFlow(scanner);
                        break;
                    case 12:
                        editCategoryFlow(scanner);
                        break;
                    case 13:
                        deleteCategoryFlow(scanner);
                        break;
                    case 14:
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

    /**
     * A helper method to get a string from the console.
     * Allows the user to type 'cancel' to abort the current operation.
     * @return The user's input, or null if they cancelled.
     */
    private static String getStringInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        if ("cancel".equalsIgnoreCase(input)) {
            return null;
        }
        return input;
    }
    
    /**
     * A robust helper to ensure an integer is entered.
     */
    private static int getIntInput(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next(); // discard non-int input
            System.out.print("Choose an option: ");
        }
        return scanner.nextInt();
    }

    /**
     * A helper method to get a double from the console.
     * Allows the user to type 'cancel' to abort.
     * @return The entered double, or -1 if they cancelled.
     */
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
    // These methods break down complex admin tasks into manageable steps.

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

        // Fetch categories from the server so the admin can choose from a list.
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

    private static void editProductFlow(Scanner scanner) throws RemoteException {
        System.out.println("\n--- Edit Product (type 'cancel' at any prompt to exit) ---");
        browseProductsAdmin(); // Show current products
        String prodIdStr = getStringInput(scanner, "Enter Product ID to edit: ");
        if (prodIdStr == null) { System.out.println("Cancelled."); return; }
        int prodId = Integer.parseInt(prodIdStr);

        // Find the product to show current values
        Product productToEdit = adminPanel.browseProducts().stream()
                .filter(p -> p.getId() == prodId)
                .findFirst()
                .orElse(null);

        if (productToEdit == null) {
            System.out.println("Product not found.");
            return;
        }

        System.out.println("Editing Product: " + productToEdit.getName() + ". Press Enter to keep the current value.");

        String name = getStringInput(scanner, "New Name [" + productToEdit.getName() + "]: ");
        if (name == null) { System.out.println("Cancelled."); return; }
        if (name.isEmpty()) name = productToEdit.getName();

        String desc = getStringInput(scanner, "New Description [" + productToEdit.getDescription() + "]: ");
        if (desc == null) { System.out.println("Cancelled."); return; }
        if (desc.isEmpty()) desc = productToEdit.getDescription();

        String priceStr = getStringInput(scanner, "New Price [" + productToEdit.getPrice() + "]: ");
        if (priceStr == null) { System.out.println("Cancelled."); return; }
        double price = priceStr.isEmpty() ? productToEdit.getPrice() : Double.parseDouble(priceStr);

        String stockStr = getStringInput(scanner, "New Stock [" + productToEdit.getStockQuantity() + "]: ");
        if (stockStr == null) { System.out.println("Cancelled."); return; }
        int stock = stockStr.isEmpty() ? productToEdit.getStockQuantity() : Integer.parseInt(stockStr);

        // Category selection
        List<Category> categories = adminPanel.getAllCategories();
        System.out.println("Select a new category [" + productToEdit.getCategory() + "]:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, categories.get(i).getName());
        }
        String catChoiceStr = getStringInput(scanner, "Choose category number (or press Enter to keep current): ");
        if (catChoiceStr == null) { System.out.println("Cancelled."); return; }
        String cat = catChoiceStr.isEmpty() ? productToEdit.getCategory() : categories.get(Integer.parseInt(catChoiceStr) - 1).getName();


        String brand = getStringInput(scanner, "New Brand [" + productToEdit.getBrand() + "]: ");
        if (brand == null) { System.out.println("Cancelled."); return; }
        if (brand.isEmpty()) brand = productToEdit.getBrand();

        String size = getStringInput(scanner, "New Size [" + productToEdit.getSize() + "]: ");
        if (size == null) { System.out.println("Cancelled."); return; }
        if (size.isEmpty()) size = productToEdit.getSize();

        String color = getStringInput(scanner, "New Color [" + productToEdit.getColor() + "]: ");
        if (color == null) { System.out.println("Cancelled."); return; }
        if (color.isEmpty()) color = productToEdit.getColor();

        Product updatedProduct = new Product(prodId, name, desc, price, stock, cat, brand, size, color);
        adminPanel.updateProduct(updatedProduct);
        System.out.println("Product updated.");
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

    private static void viewDashboardStatistics() throws RemoteException {
        String stats = adminPanel.getDashboardStatistics();
        System.out.println("\n--- Dashboard Statistics ---");
        System.out.println(stats);
    }

    private static void viewAdvancedReports() throws RemoteException {
        String report = adminPanel.getAdvancedStatisticsReport();
        System.out.println("\n--- Advanced Statistics Report ---");
        System.out.println(report);
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
        // Display all possible order statuses for the admin to choose from.
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

    private static void editCategoryFlow(Scanner scanner) throws RemoteException {
        System.out.println("\n--- Edit Category (type 'cancel' to exit) ---");
        listCategoriesAdmin(); // Show current categories
        String catIdStr = getStringInput(scanner, "\nEnter Category ID to edit: ");
        if (catIdStr == null) { System.out.println("Cancelled."); return; }
        int catId = Integer.parseInt(catIdStr);

        // Find the category to show the current name
        Category categoryToEdit = adminPanel.getAllCategories().stream()
                .filter(c -> c.getId() == catId)
                .findFirst()
                .orElse(null);

        if (categoryToEdit == null) {
            System.out.println("Category not found.");
            return;
        }

        String newCatName = getStringInput(scanner, "Enter new name for '" + categoryToEdit.getName() + "': ");
        if (newCatName == null || newCatName.trim().isEmpty()) {
            System.out.println("Cancelled or empty name provided. No changes made.");
            return;
        }

        Category updatedCategory = new Category(catId, newCatName);
        adminPanel.updateCategory(updatedCategory);
        System.out.println("Request to update category sent.");
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
