# RMI-Based Online Sports Equipment Store

This project is a distributed client-server application for an online sports equipment store, developed as part of the Distributed Systems course at the University of Beira Interior. It utilizes Java RMI (Remote Method Invocation) for communication between the client and server, with a MySQL database for data persistence.

**Theme:** T3 – Sports Equipment Store

---

## Core Technologies

-   **Java:** The primary programming language for both client and server.
-   **Java RMI:** For enabling remote procedure calls between the client and server.
-   **MySQL:** As the relational database management system (DBMS) for data persistence.
-   **JDBC:** For connecting to and interacting with the MySQL database from the Java application.

---

## Features

The application provides a console-based interface for both regular customers and administrators.

### Customer Functionalities
-   **User Authentication:** Secure registration and login for customers.
-   **Browse Products:** View a list of all available products with details like price and stock.
-   **Shopping Cart:** Add products to a personal shopping cart.
-   **Place Orders:** Convert the shopping cart into an order, which is then stored in the database.
-   **Order History:** View a history of all past orders and their current status.
-   **Real-time Notifications:** Receive instant notifications from the server (e.g., when an order's status is updated by an admin).

### Administrator Functionalities
-   **Secure Admin Login:** Separate login for administrators.
-   **Product Management:** Add new products, update the stock quantity of existing products, and delete products.
-   **Category Management:** Add new product categories, list all categories, and delete existing ones (with safety checks).
-   **Order Management:** View all orders placed by all customers and update their status (e.g., from `PENDING` to `SHIPPED`).
-   **View Statistics:** Get a report on key metrics, including:
    -   Total number of registered customers.
    -   Total number of products in the catalog.
    -   Total number of orders placed.
    -   Total sales revenue from delivered orders.

---

## Architectural Design

The project follows a three-tiered architecture and implements several key distributed systems concepts as required by the academic practical work.

-   **`common` Package:** Contains shared code, including `Remote` interfaces (e.g., `IStoreFactory`, `IUserSession`, `IAdminPanel`) and Data Transfer Objects (DTOs) like `Product`, `Customer`, and `Order`, which are `Serializable`.

-   **`server` Package:** Houses the server-side logic.
    -   **`StoreServer`:** The main entry point that starts the RMI registry and binds the factory object.
    -   **Implementations (`...Impl`)**: Concrete implementations of the remote interfaces.
    -   **`DatabaseManager`:** A utility class to manage JDBC connections to the MySQL database.
    -   **Factory Pattern:** `StoreFactoryImpl` acts as a factory to create a unique `UserSessionImpl` object for each authenticated client, ensuring session isolation.
    -   **Callback Mechanism:** The server maintains a list of active clients (`IClientCallback`) and can invoke their `notify()` method to send asynchronous messages.

-   **`client` Package:** Contains the client-side application.
    -   **`StoreClient`:** The main entry point for the user-facing console application. It looks up the remote factory from the RMI registry and implements `IClientCallback` to receive server notifications.

---

## Database Setup

The application requires a MySQL database to function.

1.  **Create the Database:** Make sure you have a MySQL server running. Create a new database with the name `rmi_onlinestore`.
    ```sql
    CREATE DATABASE IF NOT EXISTS rmi_onlinestore;
    ```
2.  **Run the SQL Script:** Execute the `query.sql` file provided in the project root. This will create the necessary tables (`customers`, `products`, `categories`, `orders`, `order_items`) and populate them with sample data.
3.  **Configure Credentials:** Open the `src/server/DatabaseManager.java` file and update the `USER` and `PASS` constants with your own MySQL username and password.

    ```java
    // src/server/DatabaseManager.java
    private static final String USER = "your_mysql_username";
    private static final String PASS = "your_mysql_password";
    ```

---

## How to Run the Application

### Prerequisites
-   Java JDK (Version 11 or higher).
-   MySQL Server.
-   MySQL Connector/J library. The project is configured to use `mysql-connector-j-9.3.0.jar` via IntelliJ's project library settings. Make sure this path is correctly configured in your IDE.

### Steps
1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/tahayunusdemir/RMIOnlineStore.git
    ```
2.  **Open in an IDE:** Open the project in an IDE like IntelliJ IDEA.
3.  **Database:** Complete the **Database Setup** steps outlined above.
4.  **Compile the Project:** Build the project using your IDE's build command.
5.  **Start the Server:** Run the `main` method in `src/server/StoreServer.java`. You should see "Server is ready." in the console.
6.  **Start the Client:** Run the `main` method in `src/client/StoreClient.java`. You can run multiple instances of the client to test concurrency.

### Default Credentials
-   **Administrator:**
    -   Username: `admin`
    -   Password: `admin`
-   **Sample Customers (from `query.sql`):**
    -   Username: `alice`, Password: `password123`
    -   Username: `bob`, Password: `securepass` 
