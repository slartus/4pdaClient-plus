package org.softeg.slartus.forpdaapi;

import android.text.Html;

import org.softeg.slartus.forpdacommon.Functions;
import org.softeg.sqliteannotations.Column;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 12:06
 */

public class Topic implements IListItem, Serializable {
    /**
     * Параметр для перехода в топике к первому непрочитанному сообщению
     */
    public static final CharSequence NAVIGATE_VIEW_LAST_URL = "getlasturl";
    /**
     * Параметр для перехода в топике к первому непрочитанному сообщению
     */
    public static final CharSequence NAVIGATE_VIEW_NEW_POST = "getnewpost";

    /**
     * Параметр для перехода в топике к первому сообщению
     */
    public static final CharSequence NAVIGATE_VIEW_FIRST_POST = "getfirstpost";

    /**
     * Параметр для перехода в топике к последнему сообщению
     */
    public static final CharSequence NAVIGATE_VIEW_LAST_POST = "getlastpost";
    public static final int FLAG_EMPTY = 0;
    public static final int FLAG_NEW = 1;

    @Column(name = "_id", isPrimaryKey = true)
    protected String m_Id;
    @Column(name = "Title")
    private String m_Title;
    @Column(name = "LastMessageDate", type = "DATE")
    private Date m_LastMessageDate;
    @Column(name = "LastMessageDateStr")
    private String m_lastMessageDateStr = null;
    @Column(name = "Flag", type = "INTEGER")
    private int m_Flag = 0;
    @Column(name = "ForumId")
    private String m_ForumId;
    @Column(name = "ForumTitle")
    private String m_ForumTitle;
    @Column(name = "Description")
    private String m_Description;
    @Column(name = "LastMessageAuthor")
    private String m_LastMessageAuthor;
    private CharSequence m_SortOrder = null;

    public Topic() {

    }

    public Topic(String id, String title) {
        this();
        m_Id = id;
        m_Title = Html.fromHtml(title).toString();
    }

    public String getId() {
        return m_Id;
    }

    @Override
    public CharSequence getTopLeft() {
        return m_LastMessageAuthor;
    }

    @Override
    public CharSequence getTopRight() {
        return m_lastMessageDateStr;
    }

    @Override
    public CharSequence getMain() {
        return m_Title;
    }

    @Override
    public CharSequence getSubMain() {
        return m_Description;
    }

    @Override
    public int getState() {
        if (getIsNew())
            return STATE_GREEN;
        return STATE_NORMAL;
    }

    @Override
    public void setState(int state) {
        switch (state) {
            case STATE_GREEN:
                setIsNew(true);
                break;
            default:
                setIsNew(false);
                break;
        }
    }

    @Override
    public CharSequence getSortOrder() {
        return m_SortOrder;
    }

    private boolean inProgress = false;

    @Override
    public boolean isInProgress() {
        return inProgress;
    }

    public void inProgress(Boolean value) {
        inProgress = value;
    }

    public void setId(String value) {
        m_Id = value;
    }

    public String getTitle() {

        return m_Title;
    }

    public void setTitle(String title) {
        setTitle(title, true);
    }

    public void setTitle(String title, Boolean fromHtml) {
        if (fromHtml && title != null) {
            m_Title = Html.fromHtml(title).toString();
        } else {
            m_Title = title;
        }
    }


    public String getLastMessageAuthor() {
        return m_LastMessageAuthor;
    }

    public void setLastMessageAuthor(String lastMessageAuthor) {
        setLastMessageAuthor(lastMessageAuthor, true);
    }

    public void setLastMessageAuthor(String lastMessageAuthor, Boolean fromHtml) {
        if (fromHtml && lastMessageAuthor != null) {
            m_LastMessageAuthor = Html.fromHtml(lastMessageAuthor).toString();
        } else {
            m_LastMessageAuthor = lastMessageAuthor;
        }
    }

    public CharSequence getLastMessageDateStr() {
        return m_lastMessageDateStr;
    }

    public Date getLastMessageDate() {
        return m_LastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        setLastMessageDate(lastMessageDate, null);
    }

    public void setLastMessageDate(Date lastMessageDate, SimpleDateFormat parseDateTimeFormat) {
        this.m_LastMessageDate = lastMessageDate;
        if (lastMessageDate == null) {
            lastMessageDate = new Date();
        }
        if (parseDateTimeFormat != null)
            m_lastMessageDateStr = parseDateTimeFormat.format(lastMessageDate);
        else
            m_lastMessageDateStr = Functions.getForumDateTime(lastMessageDate);
    }

    public String getDescription() {
        return m_Description;
    }

    public void setDescription(String description) {
        setDescription(description, true);
    }

    public void setDescription(String description, Boolean fromHtml) {
        if (fromHtml && description != null) {

            m_Description = Html.fromHtml(description).toString().trim();
        } else {
            m_Description = description;
        }
    }

    public void setIsNew(boolean aNew) {
        m_Flag = aNew ? FLAG_NEW : FLAG_EMPTY;
    }

    public boolean getIsNew() {
        return m_Flag == FLAG_NEW;
    }

    public void setFlag(int flag) {
        m_Flag = flag;
    }

    public int getFlag() {
        return m_Flag;
    }

    public String getForumId() {
        return m_ForumId;
    }

    public void setForumId(String value) {
        m_ForumId = value;
    }

    public void setForumTitle(String forumTitle) {
        this.m_ForumTitle = forumTitle;
    }

    public String getForumTitle() {
        return m_ForumTitle;
    }

    public void setSortOrder(CharSequence sortOrder) {
        m_SortOrder = sortOrder;
    }


}
