package hms.service;

import hms.model.Feedback;
import hms.storage.FeedbackRepository;

import java.time.LocalDate;
import java.util.List;

public class FeedbackService {
    private final FeedbackRepository repo = new FeedbackRepository();

    public List<Feedback> getForDoctor(String doctorId) { return repo.findByDoctor(doctorId); }
    public List<Feedback> getAll() { return repo.loadAll(); }

    public Feedback submit(String patientId, String doctorId, String appointmentId, int rating, String comments) {
        Feedback f = new Feedback(repo.nextId(), patientId, doctorId, appointmentId, rating, comments,
                LocalDate.now().toString());
        repo.save(f);
        return f;
    }

    public double averageRatingForDoctor(String doctorId) {
        List<Feedback> list = getForDoctor(doctorId);
        if (list.isEmpty()) return 0.0;
        double sum = 0;
        for (Feedback f : list) sum += f.getRating();
        return sum / list.size();
    }
}
