package org.softeg.slartus.forpdacommon;

import java.util.Iterator;
import java.util.Set;

public class CollectionUtils {
    public static String join(String sep, Set<String> set) {
        String result = null;
        if(set != null) {
            StringBuilder sb = new StringBuilder();
            Iterator<String> it = set.iterator();
            if(it.hasNext()) {
                sb.append(it.next());
            }
            while(it.hasNext()) {
                sb.append(sep).append(it.next());
            }
            result = sb.toString();
        }
        return result;
    }
}
