package db;

import model.Author;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorRepository implements CrudRepository<Author, String> {
    private static AuthorRepository instance;
    private Connection connection;

    private AuthorRepository(Connection connection) {
        this.connection = connection;
    }

    public static AuthorRepository getInstance(Connection connection) {
        if (instance == null) instance = new AuthorRepository(connection);
        return instance;
    }

    @Override
    public void create(Author author) {
        String sql = "INSERT INTO authors (name, nationality) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, author.getName());
            pstmt.setString(2, author.getNationality());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Author read(String name) {
        String sql = "SELECT * FROM authors WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return new Author(rs.getString("name"), rs.getString("nationality"));
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void update(Author author) {
        String sql = "UPDATE authors SET nationality = ? WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "Unknown");
            pstmt.setString(2, author.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(String name) {
        String sql = "DELETE FROM authors WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<Author> readAll() {
        List<Author> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM authors")) {
            while (rs.next()) list.add(new Author(rs.getString("name"), rs.getString("nationality")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}