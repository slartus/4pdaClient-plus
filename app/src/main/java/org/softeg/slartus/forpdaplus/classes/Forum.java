package org.softeg.slartus.forpdaplus.classes;

public class Forum implements ForumItem {
    private String m_Title;
    private final Forums m_Forums = new Forums();
    private final Themes m_Themes = new Themes();

    private Forum parent;
    private String tag;
    private String m_Id;


    public Forum(String id, String title) {
        m_Id = id;
        m_Title = title;
    }

    public String getTitle() {
        return m_Title;
    }

    public void setTitle(String title) {
        m_Title = title;
    }


    public void setId(String id) {
        m_Id = id;
    }

    public Themes getThemes() {
        return m_Themes;
    }

    public Forums getForums() {
        return m_Forums;
    }

    public void setParent(Forum parent) {
        this.parent = parent;
    }

    public Forum getParent() {
        return parent;
    }

    public Forum setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String getId() {
        return m_Id;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
