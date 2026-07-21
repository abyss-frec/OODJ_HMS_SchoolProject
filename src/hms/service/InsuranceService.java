package hms.service;

import hms.model.InsurancePlan;
import hms.storage.InsurancePlanRepository;

import java.util.List;

public class InsuranceService {
    private final InsurancePlanRepository repo = new InsurancePlanRepository();

    public List<InsurancePlan> getAll() { return repo.loadAll(); }

    public InsurancePlan create(String name, double coveragePercentage) {
        InsurancePlan p = new InsurancePlan(repo.nextId(), name, coveragePercentage);
        repo.save(p);
        return p;
    }

    public void update(InsurancePlan p) { repo.update(p); }
    public void delete(String id) { repo.delete(id); }
    public InsurancePlan findById(String id) { return repo.findById(id); }
}
