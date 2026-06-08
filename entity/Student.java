package entity;

public class Student {
    private String id;
    private String name;
    private String department;
    private String semester;

    public Student(String id, String name, String department, String semester) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.semester = semester;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getSemester() { return semester; }

    @Override
    public String toString() {
        return id + "," + name + "," + department + "," + semester;
    }
}
