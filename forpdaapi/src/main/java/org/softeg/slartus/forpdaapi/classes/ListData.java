package org.softeg.slartus.forpdaapi.classes;

import org.softeg.slartus.forpdaapi.IListItem;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Created by slartus on 18.10.2014.
 */
public class ListData implements Serializable {
    private int pagesCount = 1;
    private int currentPage = 1;
    private Throwable ex = null;

    private final ArrayList<IListItem> items = new ArrayList<IListItem>();

    public ArrayList<IListItem> getItems() {
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

    public Throwable getEx() {
        return ex;
    }

    public void setEx(Throwable ex) {
        this.ex = ex;
    }
}
