package hms.model;

/**
 * Medical Manager:
 * oversees a clinical department
 * manages doctor shift
 * rosters
 * views hospital metrics/revenue reports
 */
public class MedicalManager extends User {

    private String departmentId; // department this manager primarily oversees (may be "")

    public MedicalManager(String userId, String username, String password, String fullName,
                           String email, String phone, String dateRegistered, boolean active,
                           String departmentId) {
        super(userId, username, password, fullName, email, phone, dateRegistered, active);
        this.departmentId = departmentId;
    }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    @Override
    public String getRole() {
        return "Medical Manager";
    }

    @Override
    public String toFileLine() {
        return String.join("~",
                getUserId(), getUsername(), getPassword(), getFullName(),
                getEmail(), getPhone(), getDateRegistered(), String.valueOf(isActive()),
                nullSafe(departmentId));
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    public static MedicalManager fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new MedicalManager(f[0], f[1], f[2], f[3], f[4], f[5], f[6],
                Boolean.parseBoolean(f[7]), f.length > 8 ? f[8] : "");
    }
}
