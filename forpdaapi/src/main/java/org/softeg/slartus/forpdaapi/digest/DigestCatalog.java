package org.softeg.slartus.forpdaapi.digest;/*
 * Created by slinkin on 18.03.14.
 */

import android.os.Parcel;
import android.os.Parcelable;

import org.softeg.slartus.forpdaapi.ICatalogItem;
import org.softeg.sqliteannotations.Column;

import java.util.ArrayList;

public class DigestCatalog implements ICatalogItem, Parcelable {
    public static int LEVEL_TOPICS_NEXT = 0;// каталог с темами


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
    private final ArrayList<DigestCatalog> children = new ArrayList<>();
    private String htmlTitle;

    public DigestCatalog() {

    }

    public DigestCatalog(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public ArrayList<DigestCatalog> getChildren() {
        return children;
    }

    public void addChild(int i, DigestCatalog child) {
        children.add(i, child);
        child.setParent(this);
    }

    public void addChild(DigestCatalog child) {
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
        DigestCatalog clone = new DigestCatalog(id, title).setLevel(level);
        clone.setType(type);
        clone.setHtmlTitle(htmlTitle);
        if (parent != null)
            clone.parent = parent;
        for (DigestCatalog child : children) {
            clone.addChild((DigestCatalog) child.clone());
        }
        return clone;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public DigestCatalog setLevel(int level) {
        this.level = level;
        return this;
    }


    public DigestCatalog setType(int type) {
        this.type = type;
        return this;
    }

    public DigestCatalog setGames() {
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

    public static final Parcelable.Creator<DigestCatalog> CREATOR
            = new Parcelable.Creator<DigestCatalog>() {
        public DigestCatalog createFromParcel(Parcel in) {
            return new DigestCatalog(in);
        }

        public DigestCatalog[] newArray(int size) {
            return new DigestCatalog[size];
        }
    };

    private DigestCatalog(Parcel parcel) {
        id = parcel.readString();
        title = parcel.readString();
        htmlTitle = parcel.readString();
        level = parcel.readInt();
        type = parcel.readInt();
        if (parcel.readByte() == 1)
            setParent(new DigestCatalog(parcel));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        writeToParcel(parcel, i, 0);

    }

    public void writeToParcel(Parcel parcel, int i, int cnt) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(htmlTitle);
        parcel.writeInt(level);
        parcel.writeInt(type);

        if (parent == null || cnt == 2) {
            parcel.writeByte((byte) 0);
            return;
        }

        parcel.writeByte((byte) 1);
        ((DigestCatalog) parent).writeToParcel(parcel, i, cnt++);
    }
}
