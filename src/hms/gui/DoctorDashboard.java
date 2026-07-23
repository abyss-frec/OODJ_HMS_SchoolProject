package hms.gui;

import hms.model.*;
import hms.service.*;
import hms.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DoctorDashboard extends JFrame {

    private final Doctor doctor;
    private final UserService userService = new UserService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final PrescriptionService prescriptionService = new PrescriptionService();
    private final LabRequestService labRequestService = new LabRequestService();
    private final AssessmentService assessmentService = new AssessmentService();
    private final BillingService billingService = new BillingService();
    private final SystemConfigService configService = new SystemConfigService();

    public DoctorDashboard(Doctor doctor) {
        this.doctor = doctor;
        setTitle("Doctor Dashboard - " + doctor.getFullName());
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Profile", buildProfileTab());
        tabs.addTab("My Appointments", buildAppointmentsTab());
        tabs.addTab("Prescriptions", buildPrescriptionsTab());
        tabs.addTab("Lab / Imaging Requests", buildLabRequestsTab());
        tabs.addTab("Assessment Types", buildAssessmentTypesTab());
        tabs.addTab("Key In Assessment Results", buildAssessmentResultsTab());

        JPanel root = new JPanel(new BorderLayout());
        root.add(topBar(), BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel topBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel welcome = new JLabel("Logged in as: Dr. " + doctor.getFullName());
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

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 12));
        JTextField nameF = new JTextField(doctor.getFullName());
        JTextField emailF = new JTextField(doctor.getEmail());
        JTextField phoneF = new JTextField(doctor.getPhone());
        JTextField passF = new JPasswordField();
        JLabel shiftLabel = new JLabel(doctor.getShiftSchedule() + "  (set by your Medical Manager)");
        form.add(new JLabel("Full Name:")); form.add(nameF);
        form.add(new JLabel("Email:")); form.add(emailF);
        form.add(new JLabel("Phone:")); form.add(phoneF);
        form.add(new JLabel("New Password (leave blank to keep current):")); form.add(passF);
        form.add(new JLabel("Current Shift:")); form.add(shiftLabel);

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> {
            if (!Validator.isNotEmpty(nameF.getText()) || !Validator.isValidEmail(emailF.getText())
                    || !Validator.isValidPhone(phoneF.getText())) {
                DialogUtils.error(this, "Please provide a valid name, email and phone.");
                return;
            }
            doctor.setFullName(nameF.getText().trim());
            doctor.setEmail(emailF.getText().trim());
            doctor.setPhone(phoneF.getText().trim());
            if (!passF.getText().isEmpty()) doctor.setPassword(passF.getText().trim());
            userService.updateDoctor(doctor);
            DialogUtils.info(this, "Profile updated.");
        });

        panel.add(form, BorderLayout.CENTER);
        panel.add(saveBtn, BorderLayout.SOUTH);
        return panel;
    }

    // ================= APPOINTMENTS TAB =================
    private JPanel buildAppointmentsTab() {
        String[] cols = {"Appt ID", "Patient", "Date/Time", "Status", "Vitals", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Appointment a : appointmentService.getForDoctor(doctor.getUserId())) {
                Patient p = userService.getAllPatients().stream()
                        .filter(x -> x.getUserId().equals(a.getPatientId())).findFirst().orElse(null);
                model.addRow(new Object[]{a.getAppointmentId(), p != null ? p.getFullName() : a.getPatientId(),
                        a.getDateTime(), a.getStatus(), a.getVitals(), a.getConsultationNotes()});
            }
        };
        refresh.run();

        JButton completeBtn = new JButton("Log Vitals / Notes & Complete + Bill");
        JButton refreshBtn = new JButton("Refresh");

        completeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select an appointment."); return; }
            String id = (String) model.getValueAt(row, 0);
            Appointment appt = appointmentService.findById(id);
            if (appt == null) return;
            if (appt.getStatus() == Appointment.Status.COMPLETED) {
                DialogUtils.error(this, "This appointment is already completed.");
                return;
            }
            if (appt.getStatus() == Appointment.Status.CANCELLED) {
                DialogUtils.error(this, "This appointment was cancelled.");
                return;
            }

            JTextArea vitalsArea = new JTextArea(3, 25);
            JTextArea notesArea = new JTextArea(4, 25);
            JPanel p = new JPanel(new BorderLayout(6, 6));
            JPanel top = new JPanel(new GridLayout(2, 1));
            top.add(new JLabel("Vital Signs (e.g. BP 120/80, Temp 36.8C, HR 72bpm):"));
            top.add(new JScrollPane(vitalsArea));
            JPanel bottom = new JPanel(new GridLayout(2, 1));
            bottom.add(new JLabel("Consultation Notes:"));
            bottom.add(new JScrollPane(notesArea));
            p.add(top, BorderLayout.NORTH);
            p.add(bottom, BorderLayout.CENTER);

            int res = JOptionPane.showConfirmDialog(this, p, "Complete Consultation", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            if (!Validator.isNotEmpty(vitalsArea.getText()) || !Validator.isNotEmpty(notesArea.getText())) {
                DialogUtils.error(this, "Please record both vitals and consultation notes.");
                return;
            }

            appointmentService.completeConsultation(id, vitalsArea.getText().trim(), notesArea.getText().trim());
            Billing bill = billingService.generateBill(appt.getPatientId(), id, doctor.getConsultationFee());
            DialogUtils.info(this, "Consultation completed. Bill " + bill.getBillId()
                    + " generated for RM " + String.format("%.2f", bill.getFinalAmount())
                    + " (grade: " + bill.getGrade() + ").");
            refresh.run();
        });
        refreshBtn.addActionListener(e -> refresh.run());

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel btns = new JPanel();
        btns.add(completeBtn); btns.add(refreshBtn);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    // ================= PRESCRIPTIONS TAB =================
    private JPanel buildPrescriptionsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<Appointment> myAppts = appointmentService.getForDoctor(doctor.getUserId());
        JComboBox<Appointment> apptBox = new JComboBox<>(myAppts.toArray(new Appointment[0]));
        JTextArea medsArea = new JTextArea(4, 30);
        JTextArea instrArea = new JTextArea(4, 30);

        JPanel form = new JPanel(new GridLayout(3, 1, 6, 6));
        JPanel apptRow = new JPanel(new BorderLayout());
        apptRow.add(new JLabel("Appointment: "), BorderLayout.WEST);
        apptRow.add(apptBox, BorderLayout.CENTER);
        form.add(apptRow);
        JPanel medRow = new JPanel(new BorderLayout());
        medRow.add(new JLabel("Medications (one per line):"), BorderLayout.NORTH);
        medRow.add(new JScrollPane(medsArea), BorderLayout.CENTER);
        form.add(medRow);
        JPanel instrRow = new JPanel(new BorderLayout());
        instrRow.add(new JLabel("Instructions:"), BorderLayout.NORTH);
        instrRow.add(new JScrollPane(instrArea), BorderLayout.CENTER);
        form.add(instrRow);

        JButton issueBtn = new JButton("Issue Prescription");
        issueBtn.addActionListener(e -> {
            Appointment appt = (Appointment) apptBox.getSelectedItem();
            if (appt == null) { DialogUtils.error(this, "Select an appointment."); return; }
            if (!Validator.isNotEmpty(medsArea.getText())) { DialogUtils.error(this, "Please enter at least one medication."); return; }
            prescriptionService.issue(appt.getAppointmentId(), appt.getPatientId(), doctor.getUserId(),
                    medsArea.getText().trim(), instrArea.getText().trim());
            DialogUtils.info(this, "Prescription issued to patient's record.");
            medsArea.setText("");
            instrArea.setText("");
        });

        panel.add(form, BorderLayout.CENTER);
        panel.add(issueBtn, BorderLayout.SOUTH);
        return panel;
    }

    // ================= LAB REQUESTS TAB =================
    private JPanel buildLabRequestsTab() {
        String[] cols = {"Request ID", "Patient", "Type", "Status", "Date", "Room", "Result"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (LabRequest r : labRequestService.getForDoctor(doctor.getUserId())) {
                Patient p = userService.getAllPatients().stream()
                        .filter(x -> x.getUserId().equals(r.getPatientId())).findFirst().orElse(null);
                model.addRow(new Object[]{r.getRequestId(), p != null ? p.getFullName() : r.getPatientId(),
                        r.getType(), r.getStatus(), r.getDateRequested(),
                        r.getWardId().isEmpty() ? "-" : r.getWardId(), r.getResultNotes()});
            }
        };
        refresh.run();

        List<Appointment> myAppts = appointmentService.getForDoctor(doctor.getUserId());
        JComboBox<Appointment> apptBox = new JComboBox<>(myAppts.toArray(new Appointment[0]));
        JComboBox<LabRequest.Type> typeBox = new JComboBox<>(LabRequest.Type.values());
        JButton requestBtn = new JButton("New Request");
        JButton completeBtn = new JButton("Key In Result");
        JButton refreshBtn = new JButton("Refresh");

        requestBtn.addActionListener(e -> {
            JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
            p.add(new JLabel("Appointment:")); p.add(apptBox);
            p.add(new JLabel("Request Type:")); p.add(typeBox);
            int res = JOptionPane.showConfirmDialog(this, p, "Issue Lab / Imaging Request", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            Appointment appt = (Appointment) apptBox.getSelectedItem();
            if (appt == null) { DialogUtils.error(this, "Select an appointment."); return; }
            labRequestService.create(appt.getPatientId(), doctor.getUserId(), (LabRequest.Type) typeBox.getSelectedItem());
            refresh.run();
        });

        completeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a request."); return; }
            String id = (String) model.getValueAt(row, 0);
            String result = JOptionPane.showInputDialog(this, "Enter result notes:");
            if (result == null) return;
            if (!Validator.isNotEmpty(result)) { DialogUtils.error(this, "Result notes cannot be empty."); return; }
            labRequestService.complete(id, result.trim());
            refresh.run();
        });
        refreshBtn.addActionListener(e -> refresh.run());

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel btns = new JPanel();
        btns.add(requestBtn); btns.add(completeBtn); btns.add(refreshBtn);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    // ================= ASSESSMENT TYPES TAB =================
    private JPanel buildAssessmentTypesTab() {
        String[] cols = {"ID", "Name", "Unit", "Normal Range", "Critical Range"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (AssessmentType t : assessmentService.getAllTypes()) {
                model.addRow(new Object[]{t.getTypeId(), t.getName(), t.getUnit(),
                        t.getNormalMin() + " - " + t.getNormalMax(),
                        "< " + t.getCriticalMin() + " or > " + t.getCriticalMax()});
            }
        };
        refresh.run();

        JButton addBtn = new JButton("Design New Assessment Type");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> {
            JTextField nameF = new JTextField(), unitF = new JTextField(),
                    nMinF = new JTextField(), nMaxF = new JTextField(),
                    cMinF = new JTextField(), cMaxF = new JTextField();
            JPanel p = new JPanel(new GridLayout(6, 2, 6, 6));
            p.add(new JLabel("Name (e.g. Blood Pressure):")); p.add(nameF);
            p.add(new JLabel("Unit (e.g. mmHg):")); p.add(unitF);
            p.add(new JLabel("Normal Min:")); p.add(nMinF);
            p.add(new JLabel("Normal Max:")); p.add(nMaxF);
            p.add(new JLabel("Critical Min (below = critical):")); p.add(cMinF);
            p.add(new JLabel("Critical Max (above = critical):")); p.add(cMaxF);
            int res = JOptionPane.showConfirmDialog(this, p, "Design Assessment / Check-up Type", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            if (!Validator.isNotEmpty(nameF.getText()) || !Validator.isNotEmpty(unitF.getText())
                    || !Validator.isDouble(nMinF.getText()) || !Validator.isDouble(nMaxF.getText())
                    || !Validator.isDouble(cMinF.getText()) || !Validator.isDouble(cMaxF.getText())) {
                DialogUtils.error(this, "Please fill in all fields with valid numbers.");
                return;
            }
            assessmentService.createType(nameF.getText().trim(), unitF.getText().trim(),
                    Double.parseDouble(nMinF.getText()), Double.parseDouble(nMaxF.getText()),
                    Double.parseDouble(cMinF.getText()), Double.parseDouble(cMaxF.getText()), doctor.getUserId());
            refresh.run();
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a type to delete."); return; }
            String id = (String) model.getValueAt(row, 0);
            if (DialogUtils.confirm(this, "Delete this assessment type?")) {
                assessmentService.deleteType(id);
                refresh.run();
            }
        });
        refreshBtn.addActionListener(e -> refresh.run());

        return crudPanel(table, addBtn, deleteBtn, refreshBtn);
    }

    // ================= ASSESSMENT RESULTS TAB =================
    private JPanel buildAssessmentResultsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<Patient> myPatients = appointmentService.getForDoctor(doctor.getUserId()).stream()
                .map(a -> userService.getAllPatients().stream()
                        .filter(p -> p.getUserId().equals(a.getPatientId())).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        JComboBox<Patient> patientBox = new JComboBox<>(myPatients.toArray(new Patient[0]));
        JComboBox<AssessmentType> typeBox = new JComboBox<>(assessmentService.getAllTypes().toArray(new AssessmentType[0]));
        JTextField valueF = new JTextField();
        JTextArea notesArea = new JTextArea(3, 25);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.add(new JLabel("Patient:")); form.add(patientBox);
        form.add(new JLabel("Assessment Type:")); form.add(typeBox);
        form.add(new JLabel("Result Value:")); form.add(valueF);
        form.add(new JLabel("Notes:")); form.add(new JScrollPane(notesArea));

        JButton submitBtn = new JButton("Key In Result");
        submitBtn.addActionListener(e -> {
            Patient p = (Patient) patientBox.getSelectedItem();
            AssessmentType t = (AssessmentType) typeBox.getSelectedItem();
            if (p == null || t == null) { DialogUtils.error(this, "Please select a patient and assessment type."); return; }
            if (!Validator.isDouble(valueF.getText())) { DialogUtils.error(this, "Please enter a valid numeric result."); return; }
            Assessment a = assessmentService.recordResult(p.getUserId(), doctor.getUserId(), t.getTypeId(),
                    Double.parseDouble(valueF.getText().trim()), notesArea.getText().trim());
            DialogUtils.info(this, "Result recorded. Grade: " + a.getGrade());
            valueF.setText("");
            notesArea.setText("");
        });

        panel.add(form, BorderLayout.CENTER);
        panel.add(submitBtn, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crudPanel(JTable table, JButton... buttons) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel btnPanel = new JPanel();
        for (JButton b : buttons) btnPanel.add(b);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }
}
