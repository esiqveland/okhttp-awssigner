package com.github.esiqveland.okhttp3.utils;

import com.google.common.base.Charsets;

import java.io.ByteArrayInputStream;
import java.util.regex.Pattern;

import static com.github.esiqveland.okhttp3.utils.JCloudTools.hash;
import static com.google.common.io.BaseEncoding.base16;

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
     * Example: "a    b     c " → "a b c "
     *
     * @param str the string to strip
     * @return str with spaces strip
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

    public static String hexHash(String data) {
        byte[] bytes = data.getBytes(Charsets.UTF_8);
        return base16().lowerCase().encode(hash(new ByteArrayInputStream(bytes)));
    }

}
