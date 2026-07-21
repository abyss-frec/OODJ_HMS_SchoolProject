package hms.model;

/** A single medical assessment / lab result keyed in by a Doctor for a Patient. */
public class Assessment {

    private String assessmentId;
    private String patientId;
    private String doctorId;
    private String typeId;
    private String dateTime;
    private double resultValue;
    private String grade;   // NORMAL / WARNING / CRITICAL
    private String notes;

    public Assessment(String assessmentId, String patientId, String doctorId, String typeId,
                       String dateTime, double resultValue, String grade, String notes) {
        this.assessmentId = assessmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.typeId = typeId;
        this.dateTime = dateTime;
        this.resultValue = resultValue;
        this.grade = grade;
        this.notes = notes;
    }

    public String getAssessmentId() { return assessmentId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getTypeId() { return typeId; }
    public String getDateTime() { return dateTime; }
    public double getResultValue() { return resultValue; }
    public String getGrade() { return grade; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String toFileLine() {
        return String.join("~", assessmentId, patientId, doctorId, typeId, dateTime,
                String.valueOf(resultValue), grade, notes == null ? "" : notes);
    }

    public static Assessment fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new Assessment(f[0], f[1], f[2], f[3], f[4],
                Double.parseDouble(f[5]), f[6], f.length > 7 ? f[7] : "");
    }
}
