package za.ac.cput.medisnyc.util;

/* Helper.java
   Utility helper class
   Author: Phemelo Molefi (230255299)
   Date: 20 March 2026
*/

import java.util.regex.Pattern;

public class Helper {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
    public static boolean isValidPhone(String phoneNumber) {
        if (isNullOrEmpty(phoneNumber)) {
            return false;
        }return phoneNumber.matches("^0[0-9]{9}$");
    }

    public static String capitalizeFirstLetter(String str) {
        if (isNullOrEmpty(str)) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String generateId(String prefix) {
        return prefix + System.currentTimeMillis();
    }
}
