package hms.model;

/**
 * Patient:
 * books/reschedules/cancels appointments
 * views medical history and prescriptions
 * submits ratings/comments
 */
public class Patient extends User {

    private String dateOfBirth;
    private String gender;
    private String insurancePlanId; // "" if none
    private String bloodType;

    public Patient(String userId, String username, String password, String fullName,
                    String email, String phone, String dateRegistered, boolean active,
                    String dateOfBirth, String gender, String insurancePlanId, String bloodType) {
        super(userId, username, password, fullName, email, phone, dateRegistered, active);
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.insurancePlanId = insurancePlanId;
        this.bloodType = bloodType;
    }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getInsurancePlanId() { return insurancePlanId; }
    public void setInsurancePlanId(String insurancePlanId) { this.insurancePlanId = insurancePlanId; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    @Override
    public String getRole() {
        return "Patient";
    }

    @Override
    public String toFileLine() {
        return String.join("~",
                getUserId(), getUsername(), getPassword(), getFullName(),
                getEmail(), getPhone(), getDateRegistered(), String.valueOf(isActive()),
                nullSafe(dateOfBirth), nullSafe(gender), nullSafe(insurancePlanId), nullSafe(bloodType));
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    public static Patient fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new Patient(f[0], f[1], f[2], f[3], f[4], f[5], f[6],
                Boolean.parseBoolean(f[7]), f[8], f[9], f[10], f.length > 11 ? f[11] : "");
    }
}
