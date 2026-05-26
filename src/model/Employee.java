package model;


public class Employee extends User {
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

