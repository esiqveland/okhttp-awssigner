package com.github.esiqveland.awssigner.aws;

import java.util.regex.Pattern;

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

    private static Pattern noSpace = Pattern.compile("\\s+");

    /**
     * removeContiguousBlanks replaces contiguous regions of whitespace with a single space.
     * ex: "a    b     c " -> "a b c "
     * @param str
     * @return
     */
    public static String removeContiguousBlanks(String str) {
        return str == null ? null : noSpace.matcher(str).replaceAll(" ");
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
