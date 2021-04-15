package org.softeg.slartus.forpdaapi.classes;

import org.softeg.slartus.forpdaapi.Topic;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Created by slinkin on 17.03.2015.
 */
public class TopicsListData implements Serializable {
    private int pagesCount = 1;
    private int currentPage = 1;
    private Throwable error = null;

    private final ArrayList<Topic> items = new ArrayList<Topic>();

    public ArrayList<Topic> getItems() {
        return items;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable ex) {
        this.error = ex;
    }
}
