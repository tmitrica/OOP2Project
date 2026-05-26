package model;

public class Employee extends User {
    private Company company;

    public Employee(int id, String name, String email, Company company) {
        super(id, name, email);
        this.company = company;
    }

    public Company getCompany() { return company; }

    @Override
    public Subscription getSubscription() {
        return company.getSubscription();
    }

    @Override
    public String getInstitutionName() {
        return company.getName();
    }
}