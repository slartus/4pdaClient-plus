package org.softeg.slartus.forpdaapi;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 11:56
 */
public class OldUser {
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
}
