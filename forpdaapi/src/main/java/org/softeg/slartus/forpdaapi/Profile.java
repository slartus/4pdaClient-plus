package org.softeg.slartus.forpdaapi;/*
 * Created by slinkin on 17.04.2014.
 */

public class Profile {
    private CharSequence id;
    private CharSequence nick;
    private String htmlBody;
    private Throwable error;

    public CharSequence getId() {
        return id;
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


    public String getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
