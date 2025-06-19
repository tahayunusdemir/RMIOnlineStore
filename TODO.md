# Project Development Plan: RMI-Based Online Store

**Selected Theme: T3 – Sports Equipment Store**

This TODO list details the steps to be followed to develop the RMI-based "Sports Equipment Store" project.

---

### ☑ Phase 0: Project Setup (IntelliJ IDEA)

-   [x] **Create New Project:**
    1.  Open IntelliJ IDEA.
    2.  On the welcome screen or from the `File` menu, click on `New -> Project...`.
    3.  In the "New Project" window that opens:
        -   **Name:** Give your project a name (e.g., `RMIOnlineStore`).
        -   **Location:** Choose the location where the project will be saved.
        -   **Language:** `Java` should be selected.
        -   **Build system:** Select the `IntelliJ` option. This is sufficient for the simplest start.
        -   **JDK:** Select a JDK version installed on your computer (e.g., Oracle JDK 17, OpenJDK 11).
        -   Uncheck the `Add sample code` box.
    4.  Click the `Create` button.

-   [x] **Create Package Structure:**
    1.  When the project panel opens on the left, find the `src` folder.
    2.  Right-click on the `src` folder and select `New -> Package`.
    3.  In the box that opens, type `common` and press Enter.
    4.  Repeat the same process for the names `server` and `client` to create these packages as well.
    5.  Your project structure should now look like this:
        ```
        src
        ├── client
        ├── common
        └── server
        ```
---

### ☑ Phase 1: Creating the Project Structure and Common Classes

-   [x] **Create Project Directory:** Structure the project under three main Java packages:
    -   `common`: For classes that will be used by both the client and the server.
    -   `server`: For server-specific implementation classes.
    -   `client`: For classes belonging to the client application.

-   [x] **Define Data Transfer Objects (DTOs) (`common` package):**
    -   [x] `Product.java` (`Serializable`): `id`, `name` (e.g., "Nike Air Zoom Pegasus 40"), `description`, `price`, `stockQuantity`, `category`, `brand` (e.g., "Nike"), `size` (e.g., "42"), `color` (e.g., "Black").
    -   [x] `Customer.java` (`Serializable`): `id`, `username`, `password`, `name`, `address`.
    -   [x] `Order.java` (`Serializable`): `id`, `customerId`, `orderDate`, list of items, `totalAmount`, `status` (enum: `PENDING`, `SHIPPED`, `DELIVERED`).
    -   [x] `OrderItem.java` (`Serializable`): `productId`, `quantity`, `price`.
    -   [x] `Category.java` (`Serializable`): `id`, `name` (e.g., "Running Shoes", "Football Jerseys").

---

### ☑ Phase 2: Designing the Remote Interfaces (`common` package)

-   [x] **`IClientCallback.java`:** For providing feedback to the client.
-   [x] **`IUserSession.java`:** For managing user session operations.
-   [x] **`IAdminPanel.java`:** For managing administrator operations.
-   [x] **`IStoreFactory.java`:** As the main factory interface.

---

### ☑ Phase 3: Developing the Server Side (`server` package)

-   [x] **Create Implementation Classes:**
    -   [x] `UserSessionImpl.java` (extends `UnicastRemoteObject`, implements `IUserSession`).
    -   [x] `AdminPanelImpl.java` (extends `UnicastRemoteObject`, implements `IAdminPanel`).
    -   [x] `StoreFactoryImpl.java` (extends `UnicastRemoteObject`, implements `IStoreFactory`).
        -   [x] Inside this class, use a `Map<String, IClientCallback>` structure (e.g., username -> callback) to hold the `IClientCallback` references of active clients.

-   [x] **Concurrency Management (Thread Safety):**
    -   [x] As specified in the `SD_T05.md` document, multiple clients can call server methods simultaneously. Ensure that methods accessing shared data (e.g., stock quantity, order lists, callback list) are made thread-safe using the `synchronized` keyword or `java.util.concurrent` classes.

-   [x] **Database Integration (MySQL):**
    -   [x] **1. Add MySQL JDBC Driver:** Download the MySQL Connector/J `.jar` file and add it as a library to the IntelliJ project.
    -   [x] **2. Create Database and Tables:** Create a database named `rmi_onlinestore` and execute the SQL script to create `customers`, `products`, `categories`, `orders`, and `order_items` tables.
    -   [x] **3. Create `DatabaseManager.java`:** Develop a centralized class in the `server` package to manage the database connection (e.g., using Singleton pattern). This class will handle connection details and provide a static method to get the connection.
    -   [x] **4. Refactor `StoreFactoryImpl.java`:** Replace in-memory lists with JDBC calls to the database for user authentication and registration.
    -   [x] **5. Refactor `AdminPanelImpl.java`:** Implement product and stock management methods using JDBC to interact with the `products` table.
    -   [x] **6. Refactor `UserSessionImpl.java`:** Update methods like `browseProducts`, `addToCart`, `placeOrder`, and `getOrderHistory` to perform their logic using database queries instead of in-memory maps and lists.

-   [x] **Create the Main Server Class (`StoreServer.java`):**
    -   [x] Create a `main` method.
    -   [x] Start the RMI registry with `LocateRegistry.createRegistry(1099);`.
    -   [x] Create a `StoreFactoryImpl` object.
    -   [x] Register the object with `Naming.rebind("StoreFactory", factory);`.
    -   [x] Print a "Server is ready." message to the console.

---

### ☑ Phase 4: Developing the Client Side (`client` package)

-   [x] **Create the Main Client Class (`StoreClient.java`):**
    -   [x] Make the class extend `UnicastRemoteObject` and implement `IClientCallback`.
    -   [x] Implement the `notify(String message)` method to display messages from the server using `System.out.println`.
    -   [x] Inside the `main` method:
        -   [x] Wrap all remote method calls (including `lookup`) in `try-catch (RemoteException e)` blocks to handle network or server errors gracefully.
        -   [x] Connect to the server with `IStoreFactory factory = (IStoreFactory) Naming.lookup("rmi://localhost/StoreFactory");`.
        -   [x] Use `Scanner` or a similar class to get input from the user.
        -   [x] **Main Menu Loop:**
            -   [x] Display options: 1- Login, 2- Register, 3- Admin Login, 4- Exit.
            -   [x] Based on user input, make calls to `factory.login(...)`, `factory.registerCustomer(...)`, or `factory.adminLogin(...)`. Don't forget to pass the `this` reference as the callback in the `login` call.
        -   [x] **User Session Menu Loop:**
            -   [x] If login is successful, display a new menu through the `IUserSession` object: (Browse Products, My Cart, Place Order, Order History, Logout).
            -   [x] Call the relevant methods based on the selection and display the results to the user.
        -   [x] **Admin Session Menu Loop:**
            -   [x] If admin login is successful, display a new menu through the `IAdminPanel` object: (Add Product, Update Stock, View Statistics, Logout).

---

### ☐ Phase 5: Testing and Debugging

-   [ ] **Start the Server:** Run `StoreServer`.
-   [ ] **Start the Client:** Run multiple `StoreClient` instances in different terminals.
-   [ ] **Test Scenarios:**
    -   [ ] Register a new user.
    -   [ ] Log in with the registered user.
    -   [ ] List the products.
    -   [ ] **Concurrency Test:** Try to decrease the stock of the same product from two different clients at the same time and verify that the result in the database is correct.
    -   [ ] Add a product to the cart.
    -   [ ] Place an order.
    -   [ ] Log in as an administrator, add a new product (e.g., "Adidas Ultraboost" shoes).
    -   [ ] Verify that the other client sees the newly added product.
    -   [ ] **Callback Test:** Manually change the status of an order on the server side (or add an admin method for this) and check if the client receives a notification. 