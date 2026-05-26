package model;

import java.time.LocalDate;

public class Loan {
    private User user;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean isReturned;

    public Loan(User user, Book book) {
        this.user = user;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusDays(30);
        this.isReturned = false;
    }

    public User getUser() { return user; }

    public Book getBook() { return book; }

    public LocalDate getDueDate() { return dueDate; }

    public boolean isReturned() { return isReturned; }

    public void markAsReturned() { this.isReturned = true; }
}