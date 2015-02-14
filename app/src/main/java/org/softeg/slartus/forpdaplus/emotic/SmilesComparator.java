package org.softeg.slartus.forpdaplus.emotic;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by slinkin on 23.12.13.
 */
public class SmilesComparator implements Comparator<String> {

    Map<String, String> base;

    public SmilesComparator(Map<String, String> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.
    public int compare(String a, String b) {
        if (a.length() >= b.length()) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
