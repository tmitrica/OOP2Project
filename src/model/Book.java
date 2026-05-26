package model;

public class Book implements Comparable<Book> {
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
    public String getIsbn() { return isbn; }
    public int getPublicationYear() { return publicationYear; }
    public Author getAuthor() { return author; }

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
