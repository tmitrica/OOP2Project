package db;

import model.Author;
import model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookRepository implements CrudRepository<Book, String> {
    private static BookRepository instance;
    private Connection connection;

    // Constructor privat pt Singleton
    private BookRepository(Connection connection) {
        this.connection = connection;
    }

    public static BookRepository getInstance(Connection connection) {
        if (instance == null) {
            instance = new BookRepository(connection);
        }
        return instance;
    }

    @Override
    public void create(Book book) {
        String sql = "INSERT INTO books (isbn, title, publication_year, available_copies, author_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setInt(3, book.getPublicationYear());
            pstmt.setInt(4, book.getAvailableCopies());
            pstmt.setString(5, book.getAuthor().getName());
            pstmt.executeUpdate();
            System.out.println("Book '" + book.getTitle() + "' saved to database.");
        } catch (SQLException e) {
            System.err.println("Error saving book: " + e.getMessage());
        }
    }

    @Override
    public Book read(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String authorName = rs.getString("author_name");
                Author author = AuthorRepository.getInstance(connection).read(authorName);

                return new Book(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        author,
                        rs.getInt("publication_year"),
                        rs.getInt("available_copies")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error reading book: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void update(Book book) {
        String sql = "UPDATE books SET available_copies = ?, title = ?, publication_year = ?, author_name = ? WHERE isbn = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, book.getAvailableCopies());
            pstmt.setString(2, book.getTitle());
            pstmt.setInt(3, book.getPublicationYear());
            pstmt.setString(4, book.getAuthor().getName());
            pstmt.setString(5, book.getIsbn());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
        }
    }

    @Override
    public void delete(String isbn) {
        String sql = "DELETE FROM books WHERE isbn = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
        }
    }

    @Override
    public List<Book> readAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Author author = AuthorRepository.getInstance(connection).read(rs.getString("author_name"));
                books.add(new Book(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        author,
                        rs.getInt("publication_year"),
                        rs.getInt("available_copies")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error reading all books: " + e.getMessage());
        }
        return books;
    }
}