package hms.model;

/**
 * Admin Staff:
 * manages end users
 * doctor-manager assignment
 * physical hospital assets
 * consultation rates
 * insurance networks
 */
public class AdminStaff extends User {

    public AdminStaff(String userId, String username, String password, String fullName,
                       String email, String phone, String dateRegistered, boolean active) {
        super(userId, username, password, fullName, email, phone, dateRegistered, active);
    }

    @Override
    public String getRole() {
        return "Admin Staff";
    }

    @Override
    public String toFileLine() {
        return String.join("~",
                getUserId(), getUsername(), getPassword(), getFullName(),
                getEmail(), getPhone(), getDateRegistered(), String.valueOf(isActive()));
    }

    public static AdminStaff fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new AdminStaff(f[0], f[1], f[2], f[3], f[4], f[5], f[6], Boolean.parseBoolean(f[7]));
    }
}
