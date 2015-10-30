package org.softeg.slartus.forpdaplus.tabs;

import android.support.v4.app.Fragment;

/**
 * Created by radiationx on 31.10.15.
 */
public class TabItem{
    private String title;
    private String url;
    private String tag;
    private Fragment fragment;

    public TabItem(String title, String url, String tag, Fragment fragment){
        this.title = title;
        this.url = url;
        this.tag = tag;
        this.fragment = fragment;
    }

    public String getTitle() {
        return title;
    }
    public String getUrl() {
        return url;
    }
    public String getTag() {
        return tag;
    }
    public Fragment getFragment() {
        return fragment;
    }

    public void setTitle(String title){
        this.title = title;
    }
    public void setUrl(String url){
        this.url = url;
    }
}
