package org.softeg.slartus.forpdaplus.classes.forum;/*
 * Created by slinkin on 14.07.2014.
 */

import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.sqliteannotations.Column;

public class HistoryTopic extends Topic {
    @Column(name = "Url")
    protected String m_Url;
    public HistoryTopic(String id, String title, String url) {
        super(id, title);
        m_Url = url;
    }

    public String getUrl(){
        return m_Url;
    }
}
