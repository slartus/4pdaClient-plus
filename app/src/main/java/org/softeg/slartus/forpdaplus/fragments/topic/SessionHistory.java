package org.softeg.slartus.forpdaplus.fragments.topic;

import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 05.03.13
 * Time: 13:38
 * To change this template use File | Settings | File Templates.
 */
public class SessionHistory implements Serializable {
    public ExtTopic m_Topic;
public String m_Base64Body;
    public String m_Url;
    private int m_Y;

    public SessionHistory() {
        super();
    }

    public SessionHistory(ExtTopic topic,
                          String url,
                          String base64Body,
                          int y) {
        super();
        m_Topic = topic;
        m_Url = url;
        m_Base64Body = base64Body;
        m_Y = y;
    }


    public int getY() {
        return m_Y;
    }

    public String getUrl() {
        return m_Url;
    }

    public void setY(int y) {
        this.m_Y = y;
    }

    public ExtTopic getTopic() {
        return m_Topic;
    }

    public String getBody() {
        return m_Base64Body;
    }

    public void setBody(String body) {
        m_Base64Body=body;
    }
}
