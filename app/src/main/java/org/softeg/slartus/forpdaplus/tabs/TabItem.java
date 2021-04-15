package org.softeg.slartus.forpdaplus.tabs;

import androidx.fragment.app.Fragment;

/**
 * Created by radiationx on 31.10.15.
 */
public class TabItem{
    private String title;
    private String url;
    private final String tag;
    private String parentTag;
    private String subTitle;
    private Fragment fragment;

    public TabItem(String title, String url, String tag, String parentTag, Fragment fragment){
        this.title = title;
        this.url = url;
        this.tag = tag;
        this.fragment = fragment;
        this.parentTag = parentTag;
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
    public String getParentTag(){
        return parentTag;
    }
    public String getSubTitle(){
        return subTitle;
    }
    public Fragment getFragment() {
        return fragment;
    }

    public TabItem setTitle(final String title){
        this.title = title;
        return this;
    }
    public TabItem setUrl(final String url){
        this.url = url;
        return this;
    }
    public TabItem setSubTitle(final String subTitle){
        this.subTitle = subTitle;
        return this;
    }
    public TabItem setParentTag(String parentTag) {
        this.parentTag = parentTag;
        return this;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String toString(){
        return "{T="+title+", U="+url+", TT="+tag+", PT="+parentTag+", F="+fragment+"}";
    }
}
