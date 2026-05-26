package model;

public class Review {
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

    public Book getBook() { return book; }
    public int getRating() { return rating; }

    @Override
    public String toString() {
        return "Rating: " + rating + "/5 - " + comment + " (by " + employee.getName() + ")";
    }
}