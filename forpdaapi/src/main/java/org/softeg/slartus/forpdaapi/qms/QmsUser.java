package org.softeg.slartus.forpdaapi.qms;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.sqliteannotations.Column;

import java.io.Serializable;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 11:06
 */
public class QmsUser implements IListItem, Serializable {
    @Column(name = "_id", isPrimaryKey = true)
    private CharSequence id;
    @Column(name = "nick")
    private CharSequence nick;
    @Column(name = "new_messages_count")
    private CharSequence newMessagesCount = "";
    @Column(name = "messages_count")
    private final CharSequence messagesCount = "";

    private CharSequence lastThemeId = "";


    public CharSequence getNewMessagesCount() {
        return newMessagesCount;
    }

    public void setNewMessagesCount(CharSequence newMessagesCount) {
        this.newMessagesCount = newMessagesCount;
    }

    public String getId() {
        return id.toString();
    }

    @Override
    public CharSequence getTopLeft() {
        return messagesCount;
    }

    @Override
    public CharSequence getTopRight() {
        return newMessagesCount;
    }

    @Override
    public CharSequence getMain() {
        return nick;
    }

    @Override
    public CharSequence getSubMain() {
        return null;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void setState(int state) {

    }

    @Override
    public CharSequence getSortOrder() {
        return null;
    }

    @Override
    public boolean isInProgress() {
        return false;
    }

    public void setId(CharSequence id) {
        this.id = id;
    }

    public CharSequence getNick() {
        return nick;
    }

    public void setNick(CharSequence nick) {
        this.nick = nick;
    }

    public void setAvatarUrl() {
    }

    public String getLastThemeId() {
        return lastThemeId == null ? null : lastThemeId.toString();
    }

    public void setLastThemeId(CharSequence lastThemeId) {
        this.lastThemeId = lastThemeId;
    }
}
