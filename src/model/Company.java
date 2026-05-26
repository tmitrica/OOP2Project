package model;

public class Company {
    private String taxId;
    private String name;
    private String headquartersAddress;
    private Subscription subscription;

    public Company(String taxId, String name, String headquartersAddress, Subscription subscription) {
        this.taxId = taxId;
        this.name = name;
        this.headquartersAddress = headquartersAddress;
        this.subscription = subscription;
    }

    public String getName() { return name; }
    public Subscription getSubscription() { return subscription; }
    public String getTaxId() { return taxId; }
    public String getHeadquartersAddress() { return headquartersAddress; }
}
