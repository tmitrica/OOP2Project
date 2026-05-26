package model;

public abstract class User {
    protected int id;
    protected String name;
    protected String email;
    protected int currentlyBorrowedBooks;

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.currentlyBorrowedBooks = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCurrentlyBorrowedBooks() { return currentlyBorrowedBooks; }

    public void incrementBorrowedBooks() { this.currentlyBorrowedBooks++; }
    public void decrementBorrowedBooks() { this.currentlyBorrowedBooks--; }

    public abstract Subscription getSubscription();
    public abstract String getInstitutionName();
}