package org.softeg.slartus.forpdaapi;

import java.util.ArrayList;

/**
 * Created by slinkin on 26.08.13.
 */
public class Topics extends ArrayList<Topic> {

    public int getNewTopicsCount() {
        int res = 0;
        for (Topic topic : this) {
            if (topic.getIsNew())
                res++;
        }
        return res;
    }

    private int themesCount;

    public void setThemesCount(String themesCount) {
        this.themesCount = Integer.parseInt(themesCount);
    }

    public void setThemesCountInt(int themesCount) {
        this.themesCount = themesCount;
    }

    public int getThemesCount() {
        return themesCount;
    }


}
