package hms.model;

/** An accepted insurance network / plan, configured by Admin Staff. */
public class InsurancePlan {

    private String planId;
    private String name;
    private double coveragePercentage; // 0-100

    public InsurancePlan(String planId, String name, double coveragePercentage) {
        this.planId = planId;
        this.name = name;
        this.coveragePercentage = coveragePercentage;
    }

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getCoveragePercentage() { return coveragePercentage; }
    public void setCoveragePercentage(double coveragePercentage) { this.coveragePercentage = coveragePercentage; }

    public String toFileLine() {
        return String.join("~", planId, name, String.valueOf(coveragePercentage));
    }

    public static InsurancePlan fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new InsurancePlan(f[0], f[1], Double.parseDouble(f[2]));
    }

    @Override
    public String toString() {
        return name + " (" + coveragePercentage + "% coverage)";
    }
}
