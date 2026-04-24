/*
This project emulates an online app for book borrowing for companies. The 8 classes used are:
1. Subscription - the type of subscription a company has on the app
2. Company - a company that uses the app
3. Author - someone who wrote a book that is on the app
4. User - abstract class that describes a user of the app
5. Employee - inherits from user, employed at a company and can borrow books
6. Book - books that can be found on the app
7. Loan - describes what book is borrowed by whom, the period of the loan, basically a link
          between an employee and a book
8. Review - after a loan, a user can leave a review for the book

10 functionalities(methods) for the app:
1. Increment / Decrement Burrowed Books -> adjusts the number of books that an employee currently has
2. Increase / Decrease Stock -> adjusts the stock of a book
3. Compare To / To String -> used by the service class for comparing and printing books
4. Mark As Returned -> marks a loan as finalized from ongoing
5. Register Employee -> saves an employee(using his/her ID) in a dictionary for future lookups
6. Add Book -> adds a book in the Set of books
7. Burrow Book -> finds the employee by ID, checks if the books is in stock, checks the subscription type for
                  that company and creates a new loan entry in the system
8. Add Review -> check if the employee and loan exists and adds a new review for a book
9. Transfer Book To A Colleague -> an employee can transfer a book to a colleague if the loan is still active,
                                   if they are in the same company and if the colleague hasn't reached his/her
                                   subscription limit
10. Display Catalogue -> displays the entire book catalogue
 */

import java.time.LocalDate;
import java.util.*;

//exceptions used for different use cases
class OutOfStockException extends Exception {
    public OutOfStockException(String message) { super(message); }
}

class BorrowLimitExceededException extends Exception {
    public BorrowLimitExceededException(String message) { super(message); }
}

class InvalidTransferException extends Exception {
    public InvalidTransferException(String message) { super(message); }
}

class Subscription {
    private String packageName;
    private int simultaneousBooksLimit;
    private double monthlyCostPerEmployee;

    public Subscription(String packageName, int simultaneousBooksLimit, double monthlyCost) {
        this.packageName = packageName;
        this.simultaneousBooksLimit = simultaneousBooksLimit;
        this.monthlyCostPerEmployee = monthlyCost;
    }

    public int getSimultaneousBooksLimit() { return simultaneousBooksLimit; }
    public String getPackageName() { return packageName; }
}

class Company {
    private String taxId;
    private String name;
    private String headquartersAddress;
    private Subscription subscription;

    public Company(String taxId, String name, String headquartersAddress, Subscription subscription) {
        this.taxId = taxId;
        this.name = name;
        this.headquartersAddress = headquartersAddress;
        this.subscription = subscription;
    }

    public String getName() { return name; }
    public Subscription getSubscription() { return subscription; }
}

class Author {
    private String name;
    private String nationality;

    public Author(String name, String nationality) {
        this.name = name;
        this.nationality = nationality;
    }
    public String getName() { return name; }
}

abstract class User {
    protected int id;
    protected String name;
    protected String email;

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    public int getId() { return id; }
    public String getName() { return name; }
}

class Employee extends User {
    private Company company;
    private int currentlyBorrowedBooks;

    public Employee(int id, String name, String email, Company company) {
        super(id, name, email);
        this.company = company;
        this.currentlyBorrowedBooks = 0;
    }

    public Company getCompany() { return company; }
    public int getCurrentlyBorrowedBooks() { return currentlyBorrowedBooks; }

    public void incrementBorrowedBooks() { this.currentlyBorrowedBooks++; }
    public void decrementBorrowedBooks() { this.currentlyBorrowedBooks--; }
}

class Book implements Comparable<Book> {
    private String isbn;
    private String title;
    private Author author;
    private int publicationYear;
    private int availableCopies;

    public Book(String isbn, String title, Author author, int publicationYear, int availableCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.availableCopies = availableCopies;
    }

    public String getTitle() { return title; }
    public int getAvailableCopies() { return availableCopies; }

    public void decreaseStock() { this.availableCopies--; }
    public void increaseStock() { this.availableCopies++; }


    @Override
    public int compareTo(Book other) {
        int yearDiff = Integer.compare(other.publicationYear, this.publicationYear);
        if (yearDiff != 0) return yearDiff;
        return this.title.compareTo(other.title);
    }

    @Override
    public String toString() {
        return title + " (" + publicationYear + ") by " + author.getName();
    }
}

class Loan {
    private Employee employee;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean isReturned;

    public Loan(Employee employee, Book book) {
        this.employee = employee;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusDays(30);
        this.isReturned = false;
    }

    public Employee getEmployee() { return employee; }
    public Book getBook() { return book; }
    public boolean isReturned() { return isReturned; }

    public void markAsReturned() { this.isReturned = true; }
}

class Review {
    private Book book;
    private Employee employee;
    private int rating; // 1-5
    private String comment;

    public Review(Book book, Employee employee, int rating, String comment) {
        this.book = book;
        this.employee = employee;
        this.rating = Math.max(1, Math.min(5, rating));
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Rating: " + rating + "/5 - " + comment + " (by " + employee.getName() + ")";
    }
}

class PlatformService {
    private Map<Integer, User> users = new HashMap<>();

    private Set<Book> bookCatalogue = new TreeSet<>();

    private List<Loan> loanHistory = new ArrayList<>();
    private List<Review> systemReviews = new ArrayList<>();

    public void registerEmployee(Employee employee) {
        users.put(employee.getId(), employee);
    }

    public void addBook(Book book) {
        bookCatalogue.add(book);
    }

    public void borrowBook(int employeeId, Book book) throws OutOfStockException, BorrowLimitExceededException {
        Employee employee = (Employee) users.get(employeeId);

        if (employee == null) {
            throw new IllegalArgumentException("Employee not found in the database!");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new OutOfStockException("The book '" + book.getTitle() + "' is currently out of stock.");
        }

        Subscription companySubscription = employee.getCompany().getSubscription();
        if (employee.getCurrentlyBorrowedBooks() >= companySubscription.getSimultaneousBooksLimit()) {
            throw new BorrowLimitExceededException(
                    "Employee " + employee.getName() + " has reached the limit of " +
                            companySubscription.getSimultaneousBooksLimit() + " books ("
                            + companySubscription.getPackageName() + " Package)."
            );
        }

        book.decreaseStock();
        employee.incrementBorrowedBooks();

        Loan newLoan = new Loan(employee, book);
        loanHistory.add(newLoan);

        System.out.println(employee.getName() + " borrowed the book " + book.getTitle());
    }

    public void addReview(Book book, int employeeId, int rating, String message) {
        Employee employee = (Employee) users.get(employeeId);
        if(employee != null) {
            Review r = new Review(book, employee, rating, message);
            systemReviews.add(r);
            System.out.println("Review added for (" + book.getTitle() + ").");
        }
    }

    public void transferBookToColleague(int fromEmployeeId, int toEmployeeId, Book book)
            throws InvalidTransferException, BorrowLimitExceededException {

        Employee sender = (Employee) users.get(fromEmployeeId);
        Employee receiver = (Employee) users.get(toEmployeeId);

        if (sender == null || receiver == null) {
            throw new IllegalArgumentException("One or both employees not found!");
        }

        if (!sender.getCompany().getName().equals(receiver.getCompany().getName())) {
            throw new InvalidTransferException(
                    "Transfer denied: " + sender.getName() + " and " + receiver.getName() +
                            " work for different companies!"
            );
        }

        Loan activeLoan = null;
        for (Loan loan : loanHistory) {
            if (loan.getEmployee().getId() == fromEmployeeId &&
                    loan.getBook().getTitle().equals(book.getTitle()) &&
                    !loan.isReturned()) {
                activeLoan = loan;
                break;
            }
        }

        if (activeLoan == null) {
            throw new InvalidTransferException(sender.getName() + " does not have an active loan for this book.");
        }

        Subscription sub = receiver.getCompany().getSubscription();
        if (receiver.getCurrentlyBorrowedBooks() >= sub.getSimultaneousBooksLimit()) {
            throw new BorrowLimitExceededException(
                    receiver.getName() + " has reached the limit of " + sub.getSimultaneousBooksLimit() + " books."
            );
        }

        activeLoan.markAsReturned();
        sender.decrementBorrowedBooks();

        receiver.incrementBorrowedBooks();
        Loan newLoan = new Loan(receiver, book);
        loanHistory.add(newLoan);

        System.out.println("The book (" + book.getTitle() + ") was passed from "
                + sender.getName() + " directly to " + receiver.getName() + ".");
    }

    public void displayCatalogue() {
        System.out.println("\n Book catalogue: ");
        for (Book b : bookCatalogue) {
            System.out.println(b.toString() + " | Available: " + b.getAvailableCopies());
        }
    }
}


public class Main {
    public static void main(String[] args) {
        PlatformService platform = new PlatformService();

        Subscription basicPackage = new Subscription("Basic", 1, 10.0);
        Subscription premiumPackage = new Subscription("Premium", 3, 25.0);

        Company microsoft = new Company("1234", "Microsoft", "Bucharest", basicPackage);
        Company google = new Company("5678", "Google", "Bucharest", premiumPackage);

        Author jamesClear = new Author("James Clear", "USA");
        Author robertK = new Author("Robert Kiyosaki", "USA");

        Book book1 = new Book("ISBN-1", "Atomic Habits", jamesClear, 2018, 5);
        Book book2 = new Book("ISBN-2", "Rich Dad Poor Dad", robertK, 1997, 1);
        Book book3 = new Book("ISBN-3", "Clear Thinking", jamesClear, 2023, 2);

        platform.addBook(book1);
        platform.addBook(book2);
        platform.addBook(book3);

        Employee mihai = new Employee(101, "Mihai", "mihai@microsoft.ro", microsoft);
        Employee maria = new Employee(102, "Maria", "maria@google.ro", google);

        platform.registerEmployee(mihai);
        platform.registerEmployee(maria);

        platform.displayCatalogue();

        try {

            platform.borrowBook(101, book1);

            platform.borrowBook(101, book3);

        } catch (Exception e) {
            System.err.println("ERROR (Mihai): " + e.getMessage());
        }

        try {
            platform.borrowBook(102, book2);

            platform.borrowBook(102, book2);

        } catch (Exception e) {
            System.err.println("ERROR (Maria): " + e.getMessage());
        }

        Employee alex = new Employee(103, "Alex", "alex@microsoft.ro", microsoft);
        platform.registerEmployee(alex);

        try {
            platform.transferBookToColleague(101, 103, book1);

            platform.transferBookToColleague(103, 102, book1);
        } catch (Exception e) {
            System.err.println("TRANSFER ERROR: " + e.getMessage());
        }

        System.out.println();
        platform.addReview(book1, 101, 5, "It made me love life");

        platform.displayCatalogue();
    }
}