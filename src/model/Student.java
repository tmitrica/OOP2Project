package model;

public class Student extends User {
    private String university;

    private static final Subscription STUDENT_PACKAGE = new Subscription("StudentFree", 2, 0.0);

    public Student(int id, String name, String email, String university) {
        super(id, name, email);
        this.university = university;
    }

    public String getUniversity() { return university; }

    @Override
    public Subscription getSubscription() {
        return STUDENT_PACKAGE;
    }

    @Override
    public String getInstitutionName() {
        return university;
    }
}