package hms.model;

/**
 * Doctor:
 * logs vitals/notes
 * issues prescriptions
 * requests lab/imaging
 * defines assessment types
 * keys in assessment results.
 */
public class Doctor extends User {

    private String departmentId;   // specialty or department
    private String managerId;      // assigned Medical Manager
    private double consultationFee;
    private String shiftSchedule;

    public Doctor(String userId, String username, String password, String fullName,
                  String email, String phone, String dateRegistered, boolean active,
                  String departmentId, String managerId, double consultationFee, String shiftSchedule) {
        super(userId, username, password, fullName, email, phone, dateRegistered, active);
        this.departmentId = departmentId;
        this.managerId = managerId;
        this.consultationFee = consultationFee;
        this.shiftSchedule = shiftSchedule;
    }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public String getShiftSchedule() { return shiftSchedule; }
    public void setShiftSchedule(String shiftSchedule) { this.shiftSchedule = shiftSchedule; }

    @Override
    public String getRole() {
        return "Doctor";
    }

    @Override
    public String toFileLine() {
        return String.join("~",
                getUserId(), getUsername(), getPassword(), getFullName(),
                getEmail(), getPhone(), getDateRegistered(), String.valueOf(isActive()),
                nullSafe(departmentId), nullSafe(managerId),
                String.valueOf(consultationFee), nullSafe(shiftSchedule));
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    public static Doctor fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new Doctor(f[0], f[1], f[2], f[3], f[4], f[5], f[6],
                Boolean.parseBoolean(f[7]), f[8], f[9],
                f[10].isEmpty() ? 0.0 : Double.parseDouble(f[10]), f.length > 11 ? f[11] : "");
    }
}
