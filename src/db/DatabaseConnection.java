package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/BookExchangePlatform";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Mt190330";

    private static Connection connection = null;

    private DatabaseConnection() {}

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.postgresql.Driver");

                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("connection is up");

            } catch (ClassNotFoundException e) {
                System.err.println("jdbc driver not found");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("database connection error");
                e.printStackTrace();
            }
        }
        return connection;
    }
}