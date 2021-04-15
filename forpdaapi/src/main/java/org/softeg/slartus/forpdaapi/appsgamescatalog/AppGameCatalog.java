package org.softeg.slartus.forpdaapi.appsgamescatalog;/*
 * Created by slinkin on 17.03.14.
 */

import android.os.Parcel;
import android.os.Parcelable;

import org.softeg.slartus.forpdaapi.ICatalogItem;
import org.softeg.sqliteannotations.Column;

import java.util.ArrayList;

public class AppGameCatalog implements ICatalogItem, Parcelable {

    public static int LEVEL_ROOT = 0;// главная ветка
    public static int LEVEL_TYPE = 1;// игры или приложения
    public static int LEVEL_CATEGORY = 2;// категория
    public static int LEVEL_SUBCATEGORY = 3;// категория

    public static int TYPE_APPLICATIONS = 0;
    public static int TYPE_GAMES = 1;


    @Column(name = "_id", isPrimaryKey = true)
    private String id;
    @Column(name = "Title")
    private String title;
    @Column(name = "ParentId")
    public String parentId;
    @Column(name = "Level", type = "INTEGER")
    private int level = -1;
    @Column(name = "Type", type = "INTEGER")
    private int type = TYPE_APPLICATIONS;

    private ICatalogItem parent;
    private final ArrayList<AppGameCatalog> children = new ArrayList<>();
    private String htmlTitle;

    public AppGameCatalog() {

    }

    public AppGameCatalog(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public void addChild(AppGameCatalog child) {
        children.add(child);
        child.setParent(this);
    }

    @Override
    public CharSequence getId() {
        return id;
    }

    @Override
    public CharSequence getTitle() {
        return title;
    }

    @Override
    public CharSequence getSubTitle() {
        return null;
    }

    @Override
    public ICatalogItem getParent() {
        return parent;
    }

    @Override
    public void setParent(ICatalogItem parent) {

        this.parent = parent;
        if (parent != null)
            parentId = parent.getId().toString();
    }

    @Override
    public ICatalogItem clone() {
        AppGameCatalog clone = new AppGameCatalog(id, title).setLevel(level);
        clone.setType(type);
        clone.setHtmlTitle(htmlTitle);
        if (parent != null)
            clone.parent = parent;
        for (AppGameCatalog child : children) {
            clone.addChild((AppGameCatalog) child.clone());
        }
        return clone;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public AppGameCatalog setLevel(int level) {
        this.level = level;
        return this;
    }


    public AppGameCatalog setType(int type) {
        this.type = type;
        return this;
    }

    public AppGameCatalog setGames() {
        this.type = TYPE_GAMES;
        return this;
    }

    public int getType() {
        return this.type;
    }

    public String getHtmlTitle() {
        return htmlTitle;
    }

    public void setHtmlTitle(String htmlTitle) {
        this.htmlTitle = htmlTitle;
    }

    public static final Parcelable.Creator<AppGameCatalog> CREATOR
            = new Parcelable.Creator<AppGameCatalog>() {
        public AppGameCatalog createFromParcel(Parcel in) {
            return new AppGameCatalog(in);
        }

        public AppGameCatalog[] newArray(int size) {
            return new AppGameCatalog[size];
        }
    };

    private AppGameCatalog(Parcel parcel) {
        id = parcel.readString();
        title = parcel.readString();
        htmlTitle = parcel.readString();
        level = parcel.readInt();
        type = parcel.readInt();
        if (parcel.readByte() == 1)
            setParent(new AppGameCatalog(parcel));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        writeToParcel(parcel, true);

    }

    public void writeToParcel(Parcel parcel, Boolean writeParent) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(htmlTitle);
        parcel.writeInt(level);
        parcel.writeInt(type);

        if (parent == null || !writeParent) {
            parcel.writeByte((byte) 0);
            return;
        }

        parcel.writeByte((byte) 1);
        ((AppGameCatalog) parent).writeToParcel(parcel, false);
    }
}
