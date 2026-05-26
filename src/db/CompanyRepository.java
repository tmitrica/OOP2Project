package db;

import model.Company;
import model.Subscription;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompanyRepository implements CrudRepository<Company, String> {
    private static CompanyRepository instance;
    private Connection connection;

    private CompanyRepository(Connection connection) {
        this.connection = connection;
    }

    public static CompanyRepository getInstance(Connection connection) {
        if (instance == null) {
            instance = new CompanyRepository(connection);
        }
        return instance;
    }

    @Override
    public void create(Company company) {
        String sql = "INSERT INTO companies (tax_id, name, headquarters_address, subscription_name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, company.getTaxId());
            pstmt.setString(2, company.getName());
            pstmt.setString(3, company.getHeadquartersAddress());
            pstmt.setString(4, company.getSubscription().getPackageName());
            pstmt.executeUpdate();
            System.out.println("Company '" + company.getName() + "' saved to database.");
        } catch (SQLException e) {
            System.err.println("Error saving company: " + e.getMessage());
        }
    }

    @Override
    public Company read(String taxId) {
        String sql = "SELECT * FROM companies WHERE tax_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, taxId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Avem nevoie de pachet pentru a reconstrui obiectul Company
                String subName = rs.getString("subscription_name");
                Subscription sub = SubscriptionRepository.getInstance(connection).read(subName);

                return new Company(
                        rs.getString("tax_id"),
                        rs.getString("name"),
                        rs.getString("headquarters_address"),
                        sub
                );
            }
        } catch (SQLException e) {
            System.err.println("Error reading company: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void update(Company company) {
        String sql = "UPDATE companies SET name = ?, headquarters_address = ?, subscription_name = ? WHERE tax_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, company.getName());
            pstmt.setString(2, company.getHeadquartersAddress());
            pstmt.setString(3, company.getSubscription().getPackageName());
            pstmt.setString(4, company.getTaxId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating company: " + e.getMessage());
        }
    }

    @Override
    public void delete(String taxId) {
        String sql = "DELETE FROM companies WHERE tax_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, taxId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting company: " + e.getMessage());
        }
    }

    @Override
    public List<Company> readAll() {
        List<Company> companies = new ArrayList<>();
        String sql = "SELECT * FROM companies";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Subscription sub = SubscriptionRepository.getInstance(connection).read(rs.getString("subscription_name"));
                companies.add(new Company(
                        rs.getString("tax_id"),
                        rs.getString("name"),
                        rs.getString("headquarters_address"),
                        sub
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error reading all companies: " + e.getMessage());
        }
        return companies;
    }
}