package db;

import model.Subscription;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionRepository implements CrudRepository<Subscription, String> {
    private static SubscriptionRepository instance;
    private Connection connection;

    private SubscriptionRepository(Connection connection) {
        this.connection = connection;
    }

    public static SubscriptionRepository getInstance(Connection connection) {
        if (instance == null) {
            instance = new SubscriptionRepository(connection);
        }
        return instance;
    }

    @Override
    public void create(Subscription sub) {
        String sql = "INSERT INTO subscriptions (package_name, simultaneous_limit, monthly_cost) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sub.getPackageName());
            pstmt.setInt(2, sub.getSimultaneousBooksLimit());
            pstmt.setDouble(3, sub.getMonthlyCostPerEmployee());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Subscription read(String packageName) {
        String sql = "SELECT * FROM subscriptions WHERE package_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, packageName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Subscription(rs.getString("package_name"), rs.getInt("simultaneous_limit"), rs.getDouble("monthly_cost"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void update(Subscription sub) {
        String sql = "UPDATE subscriptions SET simultaneous_limit = ?, monthly_cost = ? WHERE package_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sub.getSimultaneousBooksLimit());
            pstmt.setDouble(2, 0.0);
            pstmt.setString(3, sub.getPackageName());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(String packageName) {
        String sql = "DELETE FROM subscriptions WHERE package_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, packageName);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<Subscription> readAll() {
        List<Subscription> list = new ArrayList<>();
        String sql = "SELECT * FROM subscriptions";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Subscription(rs.getString("package_name"), rs.getInt("simultaneous_limit"), rs.getDouble("monthly_cost")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}