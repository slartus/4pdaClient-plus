package org.softeg.slartus.forpdaplus.classes.common;

/**
 * User: slinkin
 * Date: 20.06.12
 * Time: 9:50
 */
public class ArrayUtils {
    public static <T> int indexOf(T needle, T[] haystack) {
        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i] != null && haystack[i].equals(needle)
                    || needle == null && haystack[i] == null) return i;
        }

        return -1;
    }

    public static int indexOf(int needle, int[] haystack) {
        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i] == needle) return i;
        }

        return -1;
    }

    public static int indexOf(String needle, String[] haystack) {
        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i].equals(needle)) return i;
        }

        return -1;
    }
}
