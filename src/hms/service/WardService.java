package hms.service;

import hms.model.Ward;
import hms.storage.WardRepository;

import java.util.List;

public class WardService {
    private final WardRepository repo = new WardRepository();

    public List<Ward> getAll() { return repo.loadAll(); }

    public Ward create(String name, Ward.Type type, int capacity, Ward.Status status) {
        Ward w = new Ward(repo.nextId(), name, type, capacity, status);
        repo.save(w);
        return w;
    }

    public void update(Ward w) { repo.update(w); }
    public void delete(String id) { repo.delete(id); }
    public Ward findById(String id) { return repo.findById(id); }

    public List<Ward> getAvailableByType(Ward.Type type) {
        return repo.loadAll().stream()
                .filter(w -> w.getType() == type && w.getStatus() == Ward.Status.AVAILABLE)
                .toList();
    }
}
