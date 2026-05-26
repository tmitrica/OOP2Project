package service;

import db.BookRepository;
import db.DatabaseConnection;
import exception.BorrowLimitExceededException;
import exception.InvalidTransferException;
import exception.OutOfStockException;
import model.*;

import java.time.LocalDate;
import java.util.*;

public class PlatformService {
    private Map<Integer, User> users = new HashMap<>();
    private List<Loan> loanHistory = new ArrayList<>();
    private List<Review> systemReviews = new ArrayList<>();

    private BookRepository bookRepository;

    private AuditService auditService;

    public PlatformService() {
        this.bookRepository = BookRepository.getInstance(DatabaseConnection.getConnection());
        this.auditService = AuditService.getInstance();
    }

    public void registerEmployee(Employee employee) {
        users.put(employee.getId(), employee);

        auditService.logAction("REGISTER_EMPLOYEE");
    }

    public void addBook(Book book) {
        bookRepository.create(book);

        auditService.logAction("ADD_BOOK");
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

        bookRepository.update(book);

        System.out.println(employee.getName() + " borrowed the book " + book.getTitle());

        auditService.logAction("BORROW_BOOK");
    }

    public void addReview(Book book, int employeeId, int rating, String message) {
        Employee employee = (Employee) users.get(employeeId);
        if(employee != null) {
            Review r = new Review(book, employee, rating, message);
            systemReviews.add(r);
            System.out.println("Review added for (" + book.getTitle() + ").");

            auditService.logAction("ADD_REVIEW");
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

        auditService.logAction("TRANSFER_BOOK");
    }

    public void displayCatalogue() {
        System.out.println("\n Book catalogue: ");

        List<Book> dbBooks = bookRepository.readAll();

        Set<Book> sortedCatalogue = new TreeSet<>(dbBooks);

        for (Book b : sortedCatalogue) {
            System.out.println(b.toString() + " | Available: " + b.getAvailableCopies());
        }

        auditService.logAction("DISPLAY_CATALOGUE");
    }

    public void generateOverdueReport() {
        System.out.println("\nOverdue books:");
        boolean foundOverdue = false;
        LocalDate today = LocalDate.now();

        for (Loan loan : loanHistory) {
            if (!loan.isReturned() && loan.getDueDate().isBefore(today)) {

                long daysLate = java.time.temporal.ChronoUnit.DAYS.between(loan.getDueDate(), today);

                System.out.println("Employee: " + loan.getEmployee().getName() +
                        " | At Company: " + loan.getEmployee().getCompany().getName() +
                        " | Book: " + loan.getBook().getTitle() +
                        " | Delay: " + daysLate);
                foundOverdue = true;
            }
        }

        if (!foundOverdue) {
            System.out.println("No overdue books");
        }

        auditService.logAction("GENERATE_OVERDUE_REPORT");
    }

    public void displayTopRatedBooks() {
        System.out.println("\n Best books based on reviews:");
        if (systemReviews.isEmpty()) {
            System.out.println("No reviews found");
            return;
        }

        Map<Book, List<Integer>> bookRatings = new HashMap<>();
        for (Review r : systemReviews) {
            bookRatings.putIfAbsent(r.getBook(), new ArrayList<>());
            bookRatings.get(r.getBook()).add(r.getRating());
        }

        Map<Book, Double> averageRatings = new HashMap<>();
        for (Map.Entry<Book, List<Integer>> entry : bookRatings.entrySet()) {
            double average = entry.getValue().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            averageRatings.put(entry.getKey(), average);
        }

        List<Map.Entry<Book, Double>> sortedEntries = new ArrayList<>(averageRatings.entrySet());
        sortedEntries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        int limit = Math.min(3, sortedEntries.size());
        for (int i = 0; i < limit; i++) {
            System.out.println((i + 1) + ". " + sortedEntries.get(i).getKey().getTitle() +
                    " | Rating: " + String.format("%.2f", sortedEntries.get(i).getValue()) + "/5.0");
        }

        auditService.logAction("DISPLAY_TOP_RATED_BOOKS");
    }
}