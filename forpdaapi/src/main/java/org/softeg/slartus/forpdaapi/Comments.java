package org.softeg.slartus.forpdaapi;

import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 28.09.12
 * Time: 9:18
 */
public class Comments extends ArrayList<Comment> {
    private int fullLength;

    public Boolean needLoadMore() {
        return getFullLength() > size();
    }

    public int getFullLength() {
        return Math.max(fullLength + 1, size());
    }

    public void setFullLength(int fullLength) {
        this.fullLength = fullLength;
    }
}
