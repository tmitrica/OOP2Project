package model;


import java.time.Instant;
import java.time.LocalDate;

public class Loan {
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

    public LocalDate getDueDate() {
        return dueDate;
    }
}

