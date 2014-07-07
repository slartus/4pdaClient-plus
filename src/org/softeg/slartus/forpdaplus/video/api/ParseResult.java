package org.softeg.slartus.forpdaplus.video.api;

import java.util.ArrayList;

/**
 * Created by slartus on 09.02.14.
 */
public class ParseResult extends VideoListItem {
    private ArrayList<VideoListItem> itemsList = new ArrayList<VideoListItem>();

    public boolean isVideoList() {
        return itemsList.size() > 0;
    }

    public ArrayList<VideoListItem> getItemsList() {
        return itemsList;
    }

    private ArrayList<VideoFormat> formats = new ArrayList<VideoFormat>();

    public ArrayList<VideoFormat> getFormats() {
        return formats;
    }

    public void setFormats(ArrayList<VideoFormat> formats) {
        this.formats = formats;
    }


}

