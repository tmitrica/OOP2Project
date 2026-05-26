/*
This project emulates an online app for book borrowing for companies and universities. The 9 classes used are:
1. Subscription - the type of subscription a company/user has on the app
2. Company - a company that uses the app
3. Author - someone who wrote a book that is on the app
4. User - abstract class that describes a user of the app
5. Employee - inherits from User, employed at a company and can borrow books
6. Student - inherits from User, enrolled at a university, has a predefined free subscription
7. Book - books that can be found on the app
8. Loan - describes what book is borrowed by whom, the period of the loan, basically a link
          between a user and a book
9. Review - after a loan, a user can leave a review for the book

10 functionalities(methods) for the app:
1. Increment / Decrement Burrowed Books -> adjusts the number of books that a user currently has
2. Increase / Decrease Stock -> adjusts the stock of a book
3. Compare To / To String -> used by the service class for comparing and printing books
4. Mark As Returned -> marks a loan as finalized from ongoing
5. Register User -> saves a user(using his/her ID) in a dictionary for future lookups
6. Add Book -> adds a book in the Set of books
7. Burrow Book -> finds the user by ID, checks if the books is in stock, checks the subscription type for
                  that user and creates a new loan entry in the system
8. Add Review -> check if the user and loan exists and adds a new review for a book
9. Transfer Book To A Peer -> a user can transfer a book to a peer if the loan is still active,
                              if they are in the same institution and if the peer hasn't reached his/her
                              subscription limit
10. Display Catalogue -> displays the entire book catalogue

Phase 2:
1. DatabaseConnection - a Singleton class that establishes and manages the JDBC connection to a PostgreSQL database.
2. CrudRepository<T, ID> - a generic interface defining standard database operations (create, read, update, delete, readAll).
3. Repository Classes (SubscriptionRepository, CompanyRepository, AuthorRepository, BookRepository) - Singleton implementations of the CrudRepository that handle specific
                                                                                                      SQL queries mapping Java objects to database tables.
4. AuditService - a Singleton service responsible for logging every major action executed in the system into a local CSV file ("platform_log.csv") alongside a timestamp.

New methods:
1. Generate Overdue Report -> iterates through all active loans, compares the due date to the current date using java.time API
                              and calculates the exact number of days a user is late returning a book
2. Display Top Rated Books -> groups all reviews by book, calculates the average rating using streams,
                              sorts the catalogue descending based on these averages and displays the top 3 books in the system.
*/

import db.AuthorRepository;
import db.CompanyRepository;
import db.DatabaseConnection;
import db.SubscriptionRepository;
import exception.BorrowLimitExceededException;
import exception.OutOfStockException;
import model.*;
import service.PlatformService;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Database initialization:");
        Connection conn = DatabaseConnection.getConnection();

        SubscriptionRepository subRepo = SubscriptionRepository.getInstance(conn);
        CompanyRepository compRepo = CompanyRepository.getInstance(conn);
        AuthorRepository authRepo = AuthorRepository.getInstance(conn);

        Subscription startupPackage = new Subscription("Startup", 1, 15.0);
        Subscription enterprisePackage = new Subscription("Enterprise", 5, 50.0);
        subRepo.create(startupPackage);
        subRepo.create(enterprisePackage);

        Company netflix = new Company("RO888", "Netflix", "Cluj", startupPackage);
        Company amazon = new Company("RO999", "Amazon", "Iasi", enterprisePackage);
        compRepo.create(netflix);
        compRepo.create(amazon);

        Author simonSinek = new Author("Simon Sinek", "UK");
        Author georgeOrwell = new Author("George Orwell", "UK");
        authRepo.create(simonSinek);
        authRepo.create(georgeOrwell);

        System.out.println("Entities were saved into db\n");


        System.out.println("Platform service checks:");
        PlatformService platform = new PlatformService();

        Book book10 = new Book("ISBN-10", "Start With Why", simonSinek, 2009, 3);
        Book book11 = new Book("ISBN-11", "1984", georgeOrwell, 1949, 1);
        Book book12 = new Book("ISBN-12", "Leaders Eat Last", simonSinek, 2014, 2);

        platform.addBook(book10);
        platform.addBook(book11);
        platform.addBook(book12);

        platform.displayCatalogue();


        System.out.println("\n Testing users (Employees & Students) and loans:");

        Employee andrei = new Employee(201, "Andrei", "andrei@amazon.ro", amazon);
        Employee elena = new Employee(202, "Elena", "elena@netflix.ro", netflix);
        Employee radu = new Employee(203, "Radu", "radu@amazon.ro", amazon);

        Student vlad = new Student(301, "Vlad", "vlad@s.unibuc.ro", "FMI Bucuresti");

        platform.registerUser(andrei);
        platform.registerUser(elena);
        platform.registerUser(radu);
        platform.registerUser(vlad);

        try {
            platform.borrowBook(201, book10); // Andrei (Enterprise limit 5)
            platform.borrowBook(202, book11); // Elena (Startup limit 1)

            // This will throw BorrowLimitExceededException for Elena
            platform.borrowBook(202, book12);
        } catch (Exception e) {
            System.err.println("LOAN ERROR (Employee): " + e.getMessage());
        }

        try {
            // Testing Student borrowing rules
            platform.borrowBook(301, book12); // Vlad borrows Leaders Eat Last
            platform.borrowBook(301, book10); // Vlad borrows Start With Why (StudentFree limit 2 reached)

            // This will throw BorrowLimitExceededException for Vlad
            platform.borrowBook(301, book11);
        } catch (Exception e) {
            System.err.println("LOAN ERROR (Student): " + e.getMessage());
        }

        try {
            System.out.println();
            platform.transferBookToPeer(201, 203, book10);
        } catch (Exception e) {
            System.err.println("TRANSFER ERROR: " + e.getMessage());
        }


        System.out.println("\n Review testing:");
        platform.addReview(book10, 203, 5, "Great concepts"); // Radu reviews Start With Why
        platform.addReview(book10, 202, 4, "Good enough, but repetitive in places"); // Elena reviews Start With Why
        platform.addReview(book11, 202, 5, "Great dystopian book");
        platform.addReview(book12, 301, 5, "Amazing leadership insights!"); // Vlad reviews Leaders Eat Last

        platform.displayTopRatedBooks();

        platform.generateOverdueReport();


        System.out.println("\n Final Db check:");
        platform.displayCatalogue();

        System.out.println("\n Check the platform_log.csv file");
    }
}