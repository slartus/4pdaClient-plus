package org.softeg.slartus.forpdaapi.users;

import org.softeg.slartus.forpdaapi.OldUser;

import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 10:53
 */
public class Users extends ArrayList<OldUser> {
    private int fullLength;
    private String tag;

    public Boolean needLoadMore() {
        return getFullLength() > size();
    }

    public int getFullLength() {
        return Math.max(fullLength + 1, size());
    }

    public void setFullLength(int fullLength) {
        this.fullLength = fullLength;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
