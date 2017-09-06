package com.github.esiqveland.awssigner.aws;

public class Utils {
    public static boolean not(boolean value) {
        return !value;
    }

    public static boolean isBlank(String s) {
        int strLen;
        if (s == null || (strLen = s.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (not(Character.isWhitespace(s.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static String defaultIfBlank(String s, String defaultValue) {
        if (isBlank(s)) {
            return defaultValue;
        } else {
            return s;
        }
    }

    public static String trim(final String str) {
        return str == null ? null : str.trim();
    }

    public static String lowerCase(final String str) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase();
    }

}
