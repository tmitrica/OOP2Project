package model;

public class Subscription {
    private String packageName;
    private int simultaneousBooksLimit;
    private double monthlyCostPerEmployee;

    public Subscription(String packageName, int simultaneousBooksLimit, double monthlyCost) {
        this.packageName = packageName;
        this.simultaneousBooksLimit = simultaneousBooksLimit;
        this.monthlyCostPerEmployee = monthlyCost;
    }

    public int getSimultaneousBooksLimit() { return simultaneousBooksLimit; }
    public String getPackageName() { return packageName; }

    public double getMonthlyCostPerEmployee() {
        return monthlyCostPerEmployee;
    }
}

