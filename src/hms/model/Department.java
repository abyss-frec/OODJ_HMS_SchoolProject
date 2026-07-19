package hms.model;

/** A clinical department / medical specialty, e.g. Cardiology. */
public class Department {

    private String deptId;
    private String name;
    private String description;
    private String headManagerId; // Medical Manager overseeing this department, may be ""

    public Department(String deptId, String name, String description, String headManagerId) {
        this.deptId = deptId;
        this.name = name;
        this.description = description;
        this.headManagerId = headManagerId;
    }

    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHeadManagerId() { return headManagerId; }
    public void setHeadManagerId(String headManagerId) { this.headManagerId = headManagerId; }

    public String toFileLine() {
        return String.join("~", deptId, name, description == null ? "" : description,
                headManagerId == null ? "" : headManagerId);
    }

    public static Department fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new Department(f[0], f[1], f.length > 2 ? f[2] : "", f.length > 3 ? f[3] : "");
    }

    @Override
    public String toString() {
        return deptId + " - " + name;
    }
}
