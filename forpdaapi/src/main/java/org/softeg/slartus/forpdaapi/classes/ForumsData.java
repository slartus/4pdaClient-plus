package org.softeg.slartus.forpdaapi.classes;

import org.softeg.slartus.forpdaapi.Forum;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by slinkin on 17.02.2015.
 */
public class ForumsData implements Serializable {
    private final int pagesCount = 1;
    private final int currentPage = 1;
    private Throwable error = null;

    private final ArrayList<Forum> items = new ArrayList<Forum>();

    public ArrayList<Forum> getItems() {
        return items;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable ex) {
        this.error = ex;
    }
}
