package org.softeg.slartus.forpdaapi;

import org.softeg.sqliteannotations.Column;

/**
 * Created by slartus on 03.06.2014.
 */
public class FavTopic  extends Topic {
    @Column(name = "tid")
    private String tid;// идентификатор избранного
    @Column(name = "trackType")
    private String trackType;
    @Column(name = "pinned",type = "BOOLEAN")
    private Boolean pinned;

    public FavTopic(){

    }
    public FavTopic(String id, String title) {
        super(id,title);
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }



    public String getTrackType() {
        return trackType;
    }

    public void setTrackType(String trackType) {
        this.trackType = trackType;
    }

    public Boolean isPinned() {
        return pinned;
    }

    public void setPinned(Boolean isPinned) {
        this.pinned = isPinned;
    }
}

