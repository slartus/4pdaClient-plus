package org.softeg.slartus.forpdaplus.classes;

import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 17.09.11
 * Time: 22:52
 * To change this template use File | Settings | File Templates.
 */
public class Forum implements ForumItem {


    private String m_Title;
    private final Forums m_Forums = new Forums();
    private final Themes m_Themes = new Themes();

    private Forum parent;
    public int level = 0;
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

    private String m_HtmlTitle;

    public void setHtmlTitle(String htmlTitle) {
        m_HtmlTitle = htmlTitle;
    }

    public String getHtmlTitle() {
        return m_HtmlTitle;
    }


    public void setId(String id) {
        m_Id = id;
    }

    public Forum addForum(Forum forum) {
        forum.level = level + 1;
        m_Forums.add(forum);
        forum.setParent(this);
        return forum;
    }

    public Forum addForum(int index, Forum forum) {
        forum.level = level + 1;
        m_Forums.add(index, forum);
        forum.setParent(this);
        return forum;
    }

    public void addTheme(ExtTopic topic) {
        m_Themes.add(topic);
    }

    public Themes getThemes() {
        return m_Themes;
    }

    public Forums getForums() {
        return m_Forums;
    }

    public Boolean LoadMore = false;

    public Forum getLastChild() {
        if (m_Forums.size() == 0)
            return this;
        return m_Forums.get(m_Forums.size() - 1);
    }

    public void clearChildren() {
        m_Forums.clear();
        m_Themes.clear();
    }

    public void setParent(Forum parent) {
        this.parent = parent;
    }

    public Forum getParent() {
        return parent;
    }

    public boolean hasChildForums() {
        return m_Forums.size() > 0;
    }

    public void getAllThemes(Themes toCollection) {
        for (int i = 0; i < m_Themes.size(); i++) {
            toCollection.add(m_Themes.get(i));
        }
        for (int i = 0; i < m_Forums.size(); i++) {
            m_Forums.get(i).getAllThemes(toCollection);
        }
    }

    public Forum findById(String startForumId, boolean recursive, Boolean themesNode) {
        int size = m_Forums.size();
        for (int i = 0; i < size; i++) {
            Forum forum = m_Forums.get(i);
            if (forum.getId().equals(startForumId)) {
                if (themesNode) {
                    Forum childResult = forum.findById(startForumId, true, false);
                    if (childResult != null) return childResult;
                }
                return forum;
            }
            if (!recursive) continue;
            Forum childResult = forum.findById(startForumId, true, themesNode);
            if (childResult != null) return childResult;
        }
        return null;
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
