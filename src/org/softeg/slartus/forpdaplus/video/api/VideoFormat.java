package org.softeg.slartus.forpdaplus.video.api;

/**
 * Format in the "fmt_list" parameter
 */
public class VideoFormat {

    private CharSequence mUrl;
    private CharSequence title;

    public CharSequence getUrl() {
        return mUrl;
    }

    public void setUrl(CharSequence url) {
        mUrl = url;
    }

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }

}
