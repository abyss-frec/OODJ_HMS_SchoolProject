package hms.util;

import java.util.regex.Pattern;

/** Centralized user-input validation. */
public class Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+\\-() ]{7,15}$");

    private Validator() { }

    public static boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isValidEmail(String s) {
        return isNotEmpty(s) && EMAIL_PATTERN.matcher(s.trim()).matches();
    }

    public static boolean isValidPhone(String s) {
        return isNotEmpty(s) && PHONE_PATTERN.matcher(s.trim()).matches();
    }

    public static boolean isDouble(String s) {
        if (!isNotEmpty(s)) return false;
        try {
            Double.parseDouble(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isInt(String s) {
        if (!isNotEmpty(s)) return false;
        try {
            Integer.parseInt(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveDouble(String s) {
        return isDouble(s) && Double.parseDouble(s.trim()) >= 0;
    }

    /** Date-time pattern check: yyyy-MM-dd HH:mm */
    public static boolean isValidDateTime(String s) {
        if (!isNotEmpty(s)) return false;
        return Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$", s.trim());
    }

    public static boolean isValidDate(String s) {
        if (!isNotEmpty(s)) return false;
        return Pattern.matches("^\\d{4}-\\d{2}-\\d{2}$", s.trim());
    }
}
