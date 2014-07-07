package org.softeg.slartus.forpdaplus.video.api;

import java.util.ArrayList;

/**
 * Created by slinkin on 30.01.14.
 */
public class YouTubeInfo {
    private ArrayList<VideoFormat> formats = new ArrayList<VideoFormat>();
    private CharSequence title;

    public ArrayList<VideoFormat> getFormats() {
        return formats;
    }

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }
}
