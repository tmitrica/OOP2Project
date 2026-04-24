# Book Renting Platform

This project emulates an application used by companies (and not only) for book borrowing. Employees, based on the company subscription type,
can borrow a certain number of books or can transfer books to employees in the same company. After reading a book, they can review it and browse the catalogue for new
readings.

## Core classes

The system has 8 main entities:

* **Subscription:** Defines the company's package, including the maximum number of books an employee can borrow simultaneously and the monthly cost.
* **Company:** A corporate entity registered on the platform, holding a specific `Subscription`.
* **Author:** The creator of a book, storing their name and nationality.
* **User:** An abstract base class defining standard platform users.
* **Employee:** Inherits from `User`. Represents a worker belonging to a specific `Company` who interacts with the book catalog.
* **Book:** Represents the physical item, tracking its metadata (ISBN, title, year) and available stock.
* **Loan:** The transactional link between an `Employee` and a `Book`, tracking the borrow date, due date, and return status.
* **Review:** Allows employees to leave a 1-to-5 star rating and a comment after reading a book.

It also has a service class that is responsible for the functionalities of the application. Some functionalities are:

1.  **Register Employee:** Adds a new employee to the system's database (using a `HashMap` for fast ID-based lookups).
2.  **Add Book:** Populates the system's catalog. Books are automatically sorted from newest to oldest using a `TreeSet`.
3.  **Display Catalogue:** Prints the entire available library alongside current stock levels.
4.  **Borrow Book:** A transactional method that finds an employee, checks book availability, verifies the company's subscription limits, adjusts the stock, and creates a new `Loan` entry.
5.  **Mark as Returned:** Finalizes an ongoing loan.
6.  **Add Review:** Allows an employee to leave feedback for a specific book.
7.  **Transfer Book to Colleague:** A unique feature allowing an employee to pass a book directly to a coworker without returning it to the library first.
8.  **Increment/Decrement Borrowed Books:** Internal state management for user limits.
9.  **Increase/Decrease Stock:** Internal state management for book inventory.
10. **Compare To & To String:** Utility methods overriding standard Java behavior for automatic sorting and clean console formatting.

Some methods offer rule enforcing through exceptions and input validation:

* **`OutOfStockException`**: Thrown if an employee attempts to borrow a book that currently has 0 available copies.
* **`BorrowLimitExceededException`**: Thrown if an employee tries to borrow a book (or receive a transfer) that exceeds the simultaneous book limit dictated by their company's `Subscription` package (e.g., Basic vs. Premium).
* **`InvalidTransferException`**: Thrown during transfers if the sender does not currently have an active loan for the book, or if the sender and receiver work for different companies.
