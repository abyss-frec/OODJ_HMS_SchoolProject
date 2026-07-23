package hms.gui;

import hms.model.*;
import hms.service.*;
import hms.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PatientDashboard extends JFrame {

    private final Patient patient;
    private final UserService userService = new UserService();
    private final DepartmentService departmentService = new DepartmentService();
    private final WardService wardService = new WardService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final PrescriptionService prescriptionService = new PrescriptionService();
    private final AssessmentService assessmentService = new AssessmentService();
    private final BillingService billingService = new BillingService();
    private final FeedbackService feedbackService = new FeedbackService();
    private final InsuranceService insuranceService = new InsuranceService();

    public PatientDashboard(Patient patient) {
        this.patient = patient;
        setTitle("Patient Portal - " + patient.getFullName());
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Profile", buildProfileTab());
        tabs.addTab("Book Appointment", buildBookingTab());
        tabs.addTab("My Appointments", buildMyAppointmentsTab());
        tabs.addTab("Medical History", buildHistoryTab());
        tabs.addTab("Billing", buildBillingTab());
        tabs.addTab("Feedback", buildFeedbackTab());

        JPanel root = new JPanel(new BorderLayout());
        root.add(topBar(), BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel topBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel welcome = new JLabel("Logged in as: " + patient.getFullName() + " (Patient)");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 13));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        bar.add(welcome, BorderLayout.WEST);
        bar.add(logout, BorderLayout.EAST);
        return bar;
    }

    // ================= PROFILE TAB =================
    private JPanel buildProfileTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));
        JTextField nameF = new JTextField(patient.getFullName());
        JTextField emailF = new JTextField(patient.getEmail());
        JTextField phoneF = new JTextField(patient.getPhone());
        JTextField passF = new JPasswordField();
        List<InsurancePlan> plans = insuranceService.getAll();
        JComboBox<InsurancePlan> insuranceBox = new JComboBox<>(plans.toArray(new InsurancePlan[0]));
        insuranceBox.insertItemAt(null, 0);
        InsurancePlan current = insuranceService.findById(patient.getInsurancePlanId());
        insuranceBox.setSelectedItem(current);
        JComboBox<String> bloodBox = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"});
        bloodBox.setSelectedItem(patient.getBloodType());

        form.add(new JLabel("Full Name:")); form.add(nameF);
        form.add(new JLabel("Email:")); form.add(emailF);
        form.add(new JLabel("Phone:")); form.add(phoneF);
        form.add(new JLabel("New Password (leave blank to keep current):")); form.add(passF);
        form.add(new JLabel("Insurance Plan:")); form.add(insuranceBox);
        form.add(new JLabel("Blood Type:")); form.add(bloodBox);

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> {
            if (!Validator.isNotEmpty(nameF.getText()) || !Validator.isValidEmail(emailF.getText())
                    || !Validator.isValidPhone(phoneF.getText())) {
                DialogUtils.error(this, "Please provide a valid name, email and phone.");
                return;
            }
            patient.setFullName(nameF.getText().trim());
            patient.setEmail(emailF.getText().trim());
            patient.setPhone(phoneF.getText().trim());
            if (!passF.getText().isEmpty()) patient.setPassword(passF.getText().trim());
            InsurancePlan sel = (InsurancePlan) insuranceBox.getSelectedItem();
            patient.setInsurancePlanId(sel == null ? "" : sel.getPlanId());
            patient.setBloodType((String) bloodBox.getSelectedItem());
            userService.updatePatient(patient);
            DialogUtils.info(this, "Profile updated.");
        });

        panel.add(form, BorderLayout.CENTER);
        panel.add(saveBtn, BorderLayout.SOUTH);
        return panel;
    }

    // ================= BOOKING TAB =================
    private JPanel buildBookingTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<Doctor> doctors = userService.getAllDoctors();
        JComboBox<Doctor> doctorBox = new JComboBox<>(doctors.toArray(new Doctor[0]));
        JTextField dateTimeF = new JTextField("2026-08-01 10:00");
        JLabel shiftLabel = new JLabel(" ");
        doctorBox.addActionListener(e -> {
            Doctor d = (Doctor) doctorBox.getSelectedItem();
            shiftLabel.setText(d != null ? "Doctor's shift: " + d.getShiftSchedule()
                    + "  |  Fee: RM " + d.getConsultationFee() : " ");
        });
        if (doctorBox.getItemCount() > 0) doctorBox.setSelectedIndex(0);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 12));
        form.add(new JLabel("Doctor:")); form.add(doctorBox);
        form.add(new JLabel("Date/Time (yyyy-MM-dd HH:mm):")); form.add(dateTimeF);
        form.add(new JLabel(" ")); form.add(shiftLabel);

        JButton bookBtn = new JButton("Book Appointment");
        bookBtn.addActionListener(e -> {
            Doctor d = (Doctor) doctorBox.getSelectedItem();
            if (d == null) { DialogUtils.error(this, "Please select a doctor."); return; }
            String dt = dateTimeF.getText().trim();
            if (!Validator.isValidDateTime(dt)) {
                DialogUtils.error(this, "Please enter date/time as yyyy-MM-dd HH:mm, e.g. 2026-08-01 10:00");
                return;
            }
            if (appointmentService.isSlotTaken(d.getUserId(), dt)) {
                DialogUtils.error(this, "That doctor already has an appointment at this time. Please choose another slot.");
                return;
            }
            List<Ward> rooms = wardService.getAvailableByType(Ward.Type.CONSULTATION_ROOM);
            String wardId = rooms.isEmpty() ? "" : rooms.get(0).getWardId();
            appointmentService.book(patient.getUserId(), d.getUserId(), wardId, dt);
            DialogUtils.info(this, "Appointment booked with Dr. " + d.getFullName() + " at " + dt + ".");
        });

        panel.add(form, BorderLayout.CENTER);
        panel.add(bookBtn, BorderLayout.SOUTH);
        return panel;
    }

    // ================= MY APPOINTMENTS TAB =================
    private JPanel buildMyAppointmentsTab() {
        String[] cols = {"Appt ID", "Doctor", "Date/Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Appointment a : appointmentService.getForPatient(patient.getUserId())) {
                Doctor d = userService.getAllDoctors().stream()
                        .filter(x -> x.getUserId().equals(a.getDoctorId())).findFirst().orElse(null);
                model.addRow(new Object[]{a.getAppointmentId(), d != null ? "Dr. " + d.getFullName() : a.getDoctorId(),
                        a.getDateTime(), a.getStatus()});
            }
        };
        refresh.run();

        JButton rescheduleBtn = new JButton("Reschedule");
        JButton cancelBtn = new JButton("Cancel");
        JButton refreshBtn = new JButton("Refresh");

        rescheduleBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select an appointment."); return; }
            String id = (String) model.getValueAt(row, 0);
            Appointment a = appointmentService.findById(id);
            if (a == null) return;
            if (a.getStatus() == Appointment.Status.COMPLETED || a.getStatus() == Appointment.Status.CANCELLED) {
                DialogUtils.error(this, "This appointment can no longer be rescheduled.");
                return;
            }
            String newDt = JOptionPane.showInputDialog(this, "New date/time (yyyy-MM-dd HH:mm):", a.getDateTime());
            if (newDt == null) return;
            if (!Validator.isValidDateTime(newDt)) { DialogUtils.error(this, "Invalid date/time format."); return; }
            if (appointmentService.isSlotTaken(a.getDoctorId(), newDt.trim())) {
                DialogUtils.error(this, "That doctor already has an appointment at this time.");
                return;
            }
            appointmentService.reschedule(id, newDt.trim());
            refresh.run();
        });

        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select an appointment."); return; }
            String id = (String) model.getValueAt(row, 0);
            if (DialogUtils.confirm(this, "Cancel this appointment?")) {
                appointmentService.cancel(id);
                refresh.run();
            }
        });
        refreshBtn.addActionListener(e -> refresh.run());

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel btns = new JPanel();
        btns.add(rescheduleBtn); btns.add(cancelBtn); btns.add(refreshBtn);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    // ================= MEDICAL HISTORY TAB =================
    private JPanel buildHistoryTab() {
        JTabbedPane inner = new JTabbedPane();
        inner.addTab("Assessment Results", buildAssessmentsHistoryPanel());
        inner.addTab("Prescriptions", buildPrescriptionsHistoryPanel());
        return wrap(inner);
    }

    private JPanel buildAssessmentsHistoryPanel() {
        String[] cols = {"Date", "Type", "Value", "Grade", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Assessment a : assessmentService.getResultsForPatient(patient.getUserId())) {
                AssessmentType t = assessmentService.findType(a.getTypeId());
                model.addRow(new Object[]{a.getDateTime(), t != null ? t.getName() : a.getTypeId(),
                        a.getResultValue() + (t != null ? " " + t.getUnit() : ""), a.getGrade(), a.getNotes()});
            }
        };
        refresh.run();
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPrescriptionsHistoryPanel() {
        String[] cols = {"Date", "Doctor", "Medications", "Instructions"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Prescription p : prescriptionService.getForPatient(patient.getUserId())) {
                Doctor d = userService.getAllDoctors().stream()
                        .filter(x -> x.getUserId().equals(p.getDoctorId())).findFirst().orElse(null);
                model.addRow(new Object[]{p.getDateIssued(), d != null ? "Dr. " + d.getFullName() : p.getDoctorId(),
                        p.getMedications(), p.getInstructions()});
            }
        };
        refresh.run();
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    // ================= BILLING TAB =================
    private JPanel buildBillingTab() {
        String[] cols = {"Bill ID", "Base Fee", "Surcharge", "Insurance Deduction", "Final Amount", "Grade", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Billing b : billingService.getForPatient(patient.getUserId())) {
                model.addRow(new Object[]{b.getBillId(), b.getBaseFee(), b.getGradeSurcharge(),
                        b.getInsuranceDeduction(), b.getFinalAmount(), b.getGrade(), b.getStatus()});
            }
        };
        refresh.run();

        JButton payBtn = new JButton("Pay Selected Bill");
        JButton refreshBtn = new JButton("Refresh");
        payBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a bill to pay."); return; }
            String id = (String) model.getValueAt(row, 0);
            String status = (String) model.getValueAt(row, 6);
            if ("PAID".equals(status)) { DialogUtils.info(this, "This bill is already paid."); return; }
            if (DialogUtils.confirm(this, "Confirm payment for bill " + id + "?")) {
                billingService.markPaid(id);
                refresh.run();
                DialogUtils.info(this, "Payment recorded. Thank you!");
            }
        });
        refreshBtn.addActionListener(e -> refresh.run());

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel btns = new JPanel();
        btns.add(payBtn); btns.add(refreshBtn);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    // ================= FEEDBACK TAB =================
    private JPanel buildFeedbackTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<Appointment> completed = appointmentService.getForPatient(patient.getUserId()).stream()
                .filter(a -> a.getStatus() == Appointment.Status.COMPLETED)
                .toList();
        JComboBox<Appointment> apptBox = new JComboBox<>(completed.toArray(new Appointment[0]));
        JComboBox<Integer> ratingBox = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        JTextArea commentsArea = new JTextArea(4, 30);

        JPanel form = new JPanel(new GridLayout(3, 1, 6, 6));
        JPanel row1 = new JPanel(new BorderLayout());
        row1.add(new JLabel("Completed Appointment: "), BorderLayout.WEST);
        row1.add(apptBox, BorderLayout.CENTER);
        form.add(row1);
        JPanel row2 = new JPanel(new BorderLayout());
        row2.add(new JLabel("Rating (1-5): "), BorderLayout.WEST);
        row2.add(ratingBox, BorderLayout.CENTER);
        form.add(row2);
        JPanel row3 = new JPanel(new BorderLayout());
        row3.add(new JLabel("Comments:"), BorderLayout.NORTH);
        row3.add(new JScrollPane(commentsArea), BorderLayout.CENTER);
        form.add(row3);

        JButton submitBtn = new JButton("Submit Feedback");
        submitBtn.addActionListener(e -> {
            Appointment a = (Appointment) apptBox.getSelectedItem();
            if (a == null) { DialogUtils.error(this, "You have no completed appointments to review yet."); return; }
            feedbackService.submit(patient.getUserId(), a.getDoctorId(), a.getAppointmentId(),
                    (Integer) ratingBox.getSelectedItem(), commentsArea.getText().trim());
            DialogUtils.info(this, "Thank you for your feedback!");
            commentsArea.setText("");
        });

        panel.add(form, BorderLayout.CENTER);
        panel.add(submitBtn, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel wrap(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(c, BorderLayout.CENTER);
        return p;
    }
}
