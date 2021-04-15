package org.softeg.slartus.forpdaplus.classes;

import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 19.09.11
 * Time: 20:46
 * To change this template use File | Settings | File Templates.
 */
public class Themes extends ArrayList<ExtTopic> {
    private int themesCount;

    @Override
    public void clear() {
        themesCount = 0;
        super.clear();
    }

    public void setThemesCount(String themesCount) {
        this.themesCount = Integer.parseInt(themesCount);
    }

    public void setThemesCountInt(int themesCount) {
        this.themesCount = themesCount;
    }

    public int getThemesCount() {
        return Math.max(themesCount, size());
    }

    public ExtTopic findByTitle(String title) {
        title = title.toLowerCase().replace(" ", "");
        for (int i = 0; i < size(); i++) {
            ExtTopic topic = get(i);
            if (topic.getTitle().replace(" ", "").equalsIgnoreCase(title))
                return topic;
        }
        return null;
    }

    public ExtTopic findById(String id) {
        for (int i = 0; i < size(); i++) {
            ExtTopic topic = get(i);
            if (topic.getId().equals(id))
                return topic;
        }
        return null;
    }


//    public Date getLastDateTheme() {
//        Date res = null;
//        for (int i=0;i<size();i++) {
//            Topic theme=get(i) ;
//            if (res == null || (theme.getLastMessageDate() != null && res.before(theme.getLastMessageDate())))
//                res = theme.getLastMessageDate();
//        }
//        return res;
//    }
}
