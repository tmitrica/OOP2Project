# Book Renting Platform

This project emulates an application used by companies and universities for book borrowing. Users (employees or students), based on their specific subscription type, can borrow a certain number of books or transfer books to peers within the same institution. After reading a book, they can review it and browse the catalogue for new readings. The system also features relational database persistence, action auditing, and polymorphism.

## Core classes

The system has 9 main entities and 4 architectural components for persistence and auditing:

* **Subscription:** Defines the package a user has, including the maximum number of books they can borrow simultaneously and the monthly cost.
* **Company:** A corporate entity registered on the platform, holding a specific `Subscription`.
* **Author:** The creator of a book, storing their name and nationality.
* **User:** An abstract base class defining standard platform users. Contains shared logic for borrowing limits and mandates abstract methods for polymorphic behavior (`getSubscription()` and `getInstitutionName()`).
* **Employee:** Inherits from `User`. Represents a worker belonging to a specific `Company` who interacts with the book catalog.
* **Student:** Inherits from `User`. Represents a learner enrolled at a university, possessing a predefined free subscription package.
* **Book:** Represents the physical item, tracking its metadata (ISBN, title, year) and available stock.
* **Loan:** The transactional link between a `User` and a `Book`, tracking the borrow date, due date, and return status.
* **Review:** Allows users to leave a 1-to-5 star rating and a comment after reading a book.
* **DatabaseConnection:** A Singleton class that establishes and manages the JDBC connection to a PostgreSQL database.
* **CrudRepository<T, ID>:** A generic interface defining standard database operations (create, read, update, delete, readAll).
* **Repository Classes:** Singleton implementations (`SubscriptionRepository`, `CompanyRepository`, `AuthorRepository`, `BookRepository`) that handle specific SQL queries mapping Java objects to database tables.
* **AuditService:** A Singleton service responsible for logging every major action executed in the system into a local CSV file (`audit_log.csv`) alongside a timestamp.

It also has a service class that is responsible for the functionalities of the application. Some functionalities are:

1.  **Register User:** Adds a new user (student or employee) to the system's database (using a `HashMap` for fast ID-based lookups).
2.  **Add Book:** Populates the system's catalog, persisting the new entry directly into the PostgreSQL database. Books are automatically sorted from newest to oldest using a `TreeSet` upon retrieval.
3.  **Display Catalogue:** Prints the entire available library alongside current stock levels, fetching the latest data from the database.
4.  **Borrow Book:** A polymorphic transactional method that finds a user, checks book availability, dynamically determines the user's subscription limits at runtime, adjusts the stock in the database, and creates a new `Loan` entry.
5.  **Mark as Returned:** Finalizes an ongoing loan.
6.  **Add Review:** Allows a user to leave feedback for a specific book.
7.  **Transfer Book to Peer:** A unique feature allowing a user to pass a book directly to a peer (e.g., coworker or fellow student) without returning it to the library first, provided they belong to the same institution.
8.  **Increment/Decrement Borrowed Books:** Internal state management for user limits.
9.  **Increase/Decrease Stock:** Internal state management for book inventory.
10. **Compare To & To String:** Utility methods overriding standard Java behavior for automatic sorting and clean console formatting.
11. **Generate Overdue Report:** Iterates through all active loans, compares the due date to the current date using the `java.time` API, and calculates the exact number of days a user is late returning a book.
12. **Display Top Rated Books:** Groups all reviews by book, calculates the average rating using streams, sorts the catalogue descending based on these averages, and displays the top 3 books in the system.
13. **Action Auditing:** Seamlessly integrated into the service; every time a business action occurs (borrow, add book, register user, transfer book, etc.), it is appended to the audit log.

Some methods offer rule enforcing through exceptions and input validation:

* **`OutOfStockException`**: Thrown if a user attempts to borrow a book that currently has 0 available copies.
* **`BorrowLimitExceededException`**: Thrown if a user tries to borrow a book (or receive a transfer) that exceeds the simultaneous book limit dictated by their specific `Subscription` package (e.g., Basic, Premium, or StudentFree).
* **`InvalidTransferException`**: Thrown during transfers if the sender does not currently have an active loan for the book, or if the sender and receiver belong to different institutions.
