package hms.model;

/**
 * Base class representing any person 
 * who can log into the HMS,
 * regardless of what role they are.
 */
public abstract class User {

    private String userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String dateRegistered;
    private boolean active;

    public User(String userId, String username, String password, String fullName,
                String email, String phone, String dateRegistered, boolean active) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dateRegistered = dateRegistered;
        this.active = active;
    }

    // ---------- Encapsulated accessors ----------
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDateRegistered() { return dateRegistered; }
    public void setDateRegistered(String dateRegistered) { this.dateRegistered = dateRegistered; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /** Every concrete subclass must declare its role name (POLYMORPHISM). */
    public abstract String getRole();

    /** Every concrete subclass knows how to serialize itself to a text-file line. */
    public abstract String toFileLine();

    @Override
    public String toString() {
        return fullName + " (" + username + ") - " + getRole();
    }
}
