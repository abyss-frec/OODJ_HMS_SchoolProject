package hms.gui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reusable helpers for building small labeled-field forms inside JOptionPane dialogs. */
public class DialogUtils {

    private DialogUtils() { }

    /**
     * Shows a simple labeled-textfield form.
     * @return map of label -> entered text, or null if the user cancelled.
     */
    public static Map<String, String> showForm(Component parent, String title, String[] labels,
                                                 String[] initialValues) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 6, 6));
        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i]));
            fields[i] = new JTextField(initialValues != null && i < initialValues.length ? initialValues[i] : "");
            panel.add(fields[i]);
        }
        int result = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return null;

        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < labels.length; i++) {
            values.put(labels[i], fields[i].getText().trim());
        }
        return values;
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}
