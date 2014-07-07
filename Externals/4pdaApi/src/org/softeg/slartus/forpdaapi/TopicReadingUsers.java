package org.softeg.slartus.forpdaapi;

import org.softeg.slartus.forpdaapi.users.Users;

/**
 * Created by slinkin on 27.06.13.
 */
public class TopicReadingUsers extends Users {
    private String hideCount;
    private String guestsCount;

    public String getHideCount() {
        return hideCount;
    }

    public void setHideCount(String hideCount) {
        this.hideCount = hideCount;
    }

    public String getGuestsCount() {
        return guestsCount;
    }

    public void setGuestsCount(String guestsCount) {
        this.guestsCount = guestsCount;
    }
}
