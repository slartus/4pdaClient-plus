package org.softeg.slartus.forpdacommon;

import java.util.regex.Pattern;

/**
 * Created by slinkin on 28.01.14.
 */
public class PatternExtensions {
    /**
     * С параметром CASE_INSENSITIVE
     *
     * @param pattern
     * @return
     */
    public static java.util.regex.Pattern compile(java.lang.String pattern) {
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }
}
