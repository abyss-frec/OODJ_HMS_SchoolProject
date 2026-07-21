package hms.service;

import hms.storage.SystemConfigRepository;

/** Admin-configurable global settings (base consultation rate, etc). */
public class SystemConfigService {
    private final SystemConfigRepository repo = new SystemConfigRepository();

    public double getBaseConsultationRate() {
        return Double.parseDouble(repo.get("baseConsultationRate", "50.00"));
    }

    public void setBaseConsultationRate(double rate) {
        repo.set("baseConsultationRate", String.valueOf(rate));
    }
}
