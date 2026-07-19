package hms.service;

import hms.model.Department;
import hms.storage.DepartmentRepository;

import java.util.List;

public class DepartmentService {
    private final DepartmentRepository repo = new DepartmentRepository();

    public List<Department> getAll() { return repo.loadAll(); }

    public Department create(String name, String description, String headManagerId) {
        Department d = new Department(repo.nextId(), name, description, headManagerId);
        repo.save(d);
        return d;
    }

    public void update(Department d) { repo.update(d); }
    public void delete(String id) { repo.delete(id); }
    public Department findById(String id) { return repo.findById(id); }
}
