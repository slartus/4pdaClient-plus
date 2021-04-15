package org.softeg.slartus.forpdaapi;

import java.io.Serializable;
import java.util.UUID;

/**
 * User: slinkin
 * Date: 25.10.11
 * Time: 9:57
 */
public class ReputationEvent implements IListItem, Serializable {

    private String userId;
    private String user;
    private String sourceUrl;
    private String source;
    private String description;
    private int state;
    private String date;

    private final String m_Id = UUID.randomUUID().toString();

    @Override
    public CharSequence getId() {
        return m_Id;
    }

    @Override
    public CharSequence getTopLeft() {
        return user;
    }

    @Override
    public CharSequence getTopRight() {
        return date;
    }

    @Override
    public CharSequence getMain() {
        return description;
    }

    @Override
    public CharSequence getSubMain() {
        return source;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void setState(int state) {
        this.state = state;
    }

    @Override
    public CharSequence getSortOrder() {
        return "";
    }

    @Override
    public boolean isInProgress() {
        return false;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public String getSource() {
        return source;
    }
}
