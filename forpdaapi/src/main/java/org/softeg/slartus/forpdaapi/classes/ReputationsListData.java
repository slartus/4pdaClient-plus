package org.softeg.slartus.forpdaapi.classes;

import java.io.Serializable;

/*
 * Created by slinkin on 19.02.2015.
 */
public class ReputationsListData extends ListData implements Serializable {
    private String user;
    private String rep;
    private String title;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRep() {
        return rep;
    }

    public void setRep(String rep) {
        this.rep = rep;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
