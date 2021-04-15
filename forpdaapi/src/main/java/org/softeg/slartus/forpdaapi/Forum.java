package org.softeg.slartus.forpdaapi;

import java.io.Serializable;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 13:37
 */
public class Forum implements Serializable {
    private String m_Id;
    private String description;
    private final String m_Title;
    private boolean hasTopics = false;
    private boolean hasForums = false;
    private String iconUrl;
    private String parentId;

    public Forum(String id, String title) {
        m_Id = id;
        m_Title = title;
    }

    public String getId() {
        return m_Id;
    }

    public String getTitle() {
        return m_Title;
    }

    @Override
    public String toString() {
        return m_Title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHasTopics() {
        return hasTopics;
    }

    public void setHasTopics(boolean hasTopics) {
        this.hasTopics = hasTopics;
    }

    public void setId(String id) {
        this.m_Id = id;
    }

    public boolean isHasForums() {
        return hasForums;
    }

    public void setHasForums(boolean hasForums) {
        this.hasForums = hasForums;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
