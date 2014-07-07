package org.softeg.slartus.forpdaplus.video.api;

/**
 * Created by slartus on 09.02.14.
 */
public class VideoListItem {
    private CharSequence title;
    private CharSequence requestUrl;
    private CharSequence videoUrl;
    private CharSequence id;

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public CharSequence getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(CharSequence requestUrl) {
        this.requestUrl = requestUrl;
    }

    public CharSequence getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(CharSequence videoUrl) {
        this.videoUrl = videoUrl;
    }

    public CharSequence getId() {
        return id;
    }

    public void setId(CharSequence id) {
        this.id = id;
    }
}
