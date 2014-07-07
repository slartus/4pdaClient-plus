package org.softeg.slartus.forpdaplus.post;/*
 * Created by slinkin on 19.03.14.
 */

import java.util.ArrayList;

public class Attaches extends ArrayList<Attach> {
    public String getFileList() {
        String res = "0";
        for (Attach attach : this) {
            res += "," + attach.getId();
        }
        return res;
    }
}
