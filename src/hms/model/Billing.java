package hms.model;

/**
 * A bill generated for a completed appointment. Combines the medical
 * grading system (surcharge applied when the visit's grade is WARNING or
 * CRITICAL) with the patient's insurance coverage to compute a final amount.
 */
public class Billing {

    public enum Status { UNPAID, PAID }

    private String billId;
    private String patientId;
    private String appointmentId;
    private double baseFee;
    private double gradeSurcharge;
    private double insuranceDeduction;
    private double finalAmount;
    private String grade;       // overall grade used for this bill (NORMAL/WARNING/CRITICAL/N-A)
    private Status status;
    private String dateIssued;

    public Billing(String billId, String patientId, String appointmentId, double baseFee,
                    double gradeSurcharge, double insuranceDeduction, double finalAmount,
                    String grade, Status status, String dateIssued) {
        this.billId = billId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.baseFee = baseFee;
        this.gradeSurcharge = gradeSurcharge;
        this.insuranceDeduction = insuranceDeduction;
        this.finalAmount = finalAmount;
        this.grade = grade;
        this.status = status;
        this.dateIssued = dateIssued;
    }

    public String getBillId() { return billId; }
    public String getPatientId() { return patientId; }
    public String getAppointmentId() { return appointmentId; }
    public double getBaseFee() { return baseFee; }
    public double getGradeSurcharge() { return gradeSurcharge; }
    public double getInsuranceDeduction() { return insuranceDeduction; }
    public double getFinalAmount() { return finalAmount; }
    public String getGrade() { return grade; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getDateIssued() { return dateIssued; }

    public String toFileLine() {
        return String.join("~", billId, patientId, appointmentId,
                String.valueOf(baseFee), String.valueOf(gradeSurcharge), String.valueOf(insuranceDeduction),
                String.valueOf(finalAmount), grade, status.name(), dateIssued);
    }

    public static Billing fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new Billing(f[0], f[1], f[2], Double.parseDouble(f[3]), Double.parseDouble(f[4]),
                Double.parseDouble(f[5]), Double.parseDouble(f[6]), f[7], Status.valueOf(f[8]), f[9]);
    }
}
