package hms.model;

/** A digital medication prescription issued by a Doctor to a Patient. */
public class Prescription {

    private String prescriptionId;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String medications; // free text, semicolon separated, can be multiple
    private String instructions;
    private String dateIssued;

    public Prescription(String prescriptionId, String appointmentId, String patientId, String doctorId,
                         String medications, String instructions, String dateIssued) {
        this.prescriptionId = prescriptionId;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.medications = medications;
        this.instructions = instructions;
        this.dateIssued = dateIssued;
    }

    public String getPrescriptionId() { return prescriptionId; }
    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getMedications() { return medications; }
    public String getInstructions() { return instructions; }
    public String getDateIssued() { return dateIssued; }

    public String toFileLine() {
        return String.join("~", prescriptionId, appointmentId, patientId, doctorId,
                medications.replace("~", ";"), instructions.replace("~", ";"), dateIssued);
    }

    public static Prescription fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new Prescription(f[0], f[1], f[2], f[3], f[4], f[5], f[6]);
    }
}
