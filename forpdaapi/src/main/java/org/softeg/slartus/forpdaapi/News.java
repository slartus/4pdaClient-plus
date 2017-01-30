package org.softeg.slartus.forpdaapi;

import org.softeg.sqliteannotations.Column;

/**
 * Created by slinkin on 29.07.13.
 */
public class News implements IListItem {
    @Column(name = "_id", isPrimaryKey = true)
    private CharSequence id;

    @Column(name = "title")
    private CharSequence title;

    @Column(name = "newsDate")
    private String newsDate;

    @Column(name = "author")
    private CharSequence author;

    @Column(name = "description")
    private CharSequence description;

    @Column(name = "imgUrl")
    private CharSequence imgUrl;

    @Column(name = "page", type = "INTEGER")
    private int page;

    @Column(name = "tagLink")
    private CharSequence tagLink;

    @Column(name = "tagName")
    private CharSequence tagName;

    @Column(name = "tagTitle")
    private CharSequence tagTitle;

    @Column(name = "sourceTitle")
    private CharSequence sourceTitle;

    @Column(name = "sourceUrl")
    private CharSequence sourceUrl;

    @Column(name = "commentsCount", type = "INTEGER")
    private int commentsCount;

    public News() {

    }

    public News(CharSequence id, CharSequence title) {
        super();
        this.id = id;
        this.title = title;
    }


    public CharSequence getId() {
        return id;
    }

    @Override
    public CharSequence getTopLeft() {
        return getAuthor();
    }

    @Override
    public CharSequence getTopRight() {
        return getNewsDate();
    }

    @Override
    public CharSequence getMain() {
        return getTitle();
    }

    @Override
    public CharSequence getSubMain() {
        return getDescription();
    }

    @Override
    public int getState() {
        return STATE_NORMAL;
    }

    @Override
    public void setState(int state) {

    }

    @Override
    public CharSequence getSortOrder() {
        return null;
    }

    @Override
    public boolean isInProgress() {
        return false;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNewsDate() {
        return newsDate;
    }

    public void setNewsDate(String newsDate) {
        this.newsDate = newsDate;
    }

    public CharSequence getAuthor() {
        return author;
    }

    public void setAuthor(CharSequence author) {
        this.author = author;
    }

    public CharSequence getDescription() {
        return description;
    }

    public void setDescription(CharSequence description) {
        this.description = description;
    }

    public CharSequence getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(CharSequence imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    /**
     * Ссылка на раздел
     */
    public CharSequence getTagLink() {
        return tagLink;
    }

    public void setTagLink(CharSequence tagLink) {
        this.tagLink = tagLink;
    }

    /**
     * Имя раздела
     */
    public CharSequence getTagName() {
        return tagName;
    }

    public void setTagName(CharSequence tagName) {
        this.tagName = tagName;
    }

    /**
     * Название раздела
     */
    public CharSequence getTagTitle() {
        return tagTitle;
    }

    public void setTagTitle(CharSequence tagTitle) {
        this.tagTitle = tagTitle;
    }

	/*
     *Количество комментариев
	 */

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

	/*
     * Url источника
	 */

    public void setSourceUrl(CharSequence sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public CharSequence getSourceUrl() {
        return sourceUrl;
    }

	/*
     * Текст источника
	 */


    public void setSourceTitle(CharSequence sourceTitle) {
        this.sourceTitle = sourceTitle;
    }

    public CharSequence getSourceTitle() {
        return sourceTitle;
    }


    public String getUrl() {
        return getId().toString().replaceAll("([^:])//", "$1/");
    }
}
