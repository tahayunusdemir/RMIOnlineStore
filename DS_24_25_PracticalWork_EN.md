University of Beira Interior - Distributed Systems – 2024/2025

**Practical Work – Distributed Application using Java RMI**

**Description:**

The objective of this work is to develop a client-server based distributed application for an Online Store using Java RMI (Remote Method Invocation) technology. The server side will manage the business logic and data persistence, while the client side will provide users with access to the system. For data persistence, a DBMS (Database Management System) chosen by the group will be used.

Choose a business area from the following set of themes (each theme can be chosen by a maximum of 4 groups):

T1 – Electronic Home Appliances Store;
T2 – Furniture Store (home and garden);
T3 – Sports Equipment Store;
T4 – Motor Vehicles Store;
T5 – Food Products Store;
T6 – Health Products Store

**Core Functionalities:**

The products to be marketed must be grouped into categories to present a structured product catalog. The application must offer the following functions on the server via RMI methods:
-   **Product Management:** Update products and categories (add, delete, edit).
-   **Stock and Sales Management:** Allow various types of queries, and manage sales and current stocks.
-   **Customer Operations:** Registered customers should be able to make online purchases through the client application.
-   **Invoicing:** For each sale of a product or set of products, an invoice information must be generated for the customer, indicating that payment will be made upon delivery of the product.
-   **Statistics:** The server must provide methods capable of generating statistics of the following types: Best/least selling products, top customers, turnover in a specific period (month/week/day).

**Technical Requirements and Architectural Approach:**

The following RMI-based architecture and concepts must be addressed in the development of the application:

1.  **Distributed Architecture:**
    *   **Server:** The main Java application that contains all business logic, performs database operations, and publishes remote objects in the RMI Registry.
    *   **Client:** The Java application (preferably console-based) with which users interact. It accesses server objects through the RMI Registry to call remote methods.
    *   **Common Module:** Contains the remote interfaces (`Remote` interface) and data transfer objects (`Serializable` classes) to be shared by the client and server.

2.  **Remote Interfaces:** The services to be provided by the server must be defined with interfaces that extend the `java.rmi.Remote` interface. For example: `IStore`, `IAdminPanel`, `IUserSession`.

3.  **Data Transfer Objects (DTOs):** Data such as Product, Customer, Order must be transferred between the client and server through classes (POJOs) that implement the `java.io.Serializable` interface.

4.  **Session Management (with Factory Pattern):** The "Factory" design pattern should be used to create a specific session object for each client upon user login (as in `SD_FP07.md`). An `IStoreFactory` interface can produce personal `IUserSession` objects for clients. This allows each user to manage their own shopping cart and state.

5.  **Asynchronous Notifications (with Callback):** The "Callback" mechanism should be used to send instant notifications from the server to the client (as in `SD_FP07.md`). For example, when an order's status changes (e.g., "shipped"), the server can notify the respective client by calling back one of its methods.

6.  **Security:**
    *   **Authentication:** Users must be required to log in to the system.
    *   **Authorization:** Certain methods (e.g., adding products, viewing statistics) should only be callable by users with an administrator (admin) role.

**Good luck with your work** 