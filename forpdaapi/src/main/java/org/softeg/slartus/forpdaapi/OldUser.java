package org.softeg.slartus.forpdaapi;

import java.io.Serializable;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 11:56
 */
public class OldUser implements IListItem, Serializable {
    private String nick = null;
    private String avatarUrl = null;
    private String mid = null;
    private String tag = null;
    public String MessagesCount = null;
    public String State = null;
    public String LastVisit = null;
    public String Group = null;

    public String getNick() {
        return nick;
    }

    private String htmlColor = "gray";

    public String getHtmlColor() {
        return htmlColor;
    }

    public void setHtmlColor(String htmlColor) {
        this.htmlColor = htmlColor;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }


    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public CharSequence getId() {
        return null;
    }

    @Override
    public CharSequence getTopLeft() {
        return null;
    }

    @Override
    public CharSequence getTopRight() {
        return null;
    }

    @Override
    public CharSequence getMain() {
        return null;
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
}
