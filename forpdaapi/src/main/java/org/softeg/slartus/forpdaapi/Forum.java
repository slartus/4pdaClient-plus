package org.softeg.slartus.forpdaapi;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 13:37
 */
public class Forum implements ICatalogItem, Parcelable {
    protected String m_Id;
    private CharSequence description;
    private ICatalogItem parent;
    protected String m_Title;
    private boolean hasTopics;
    public int level = 0;
    private String parentId;

    public Forum(String id, String title) {
        m_Id = id;
        m_Title = title;
    }

    public String getId() {
        return m_Id;
    }

    public String getTitle() {
        return m_Title;
    }

    @Override
    public CharSequence getSubTitle() {
        return description;
    }


    @Override
    public String toString() {
        return m_Title.toString();
    }

    @Override
    public ICatalogItem getParent() {
        return parent;
    }

    @Override
    public void setParent(ICatalogItem catalogItem) {
        this.parent = catalogItem;
    }

    @Override
    public ICatalogItem clone() {
        Forum clone = new Forum(m_Id, m_Title);
        clone.setDescription(description);
        clone.setHasTopics(hasTopics);
        clone.setParent(parent == null ? null : parent.clone());
        return clone;
    }

    public CharSequence getDescription() {
        return description;
    }

    public void setDescription(CharSequence description) {
        this.description = description;
    }

    public boolean isHasTopics() {
        return hasTopics;
    }

    public void setHasTopics(boolean hasTopics) {
        this.hasTopics = hasTopics;
    }

    public void setId(String id) {
        this.m_Id = id;
    }

    public static final Parcelable.Creator<Forum> CREATOR = new Parcelable.Creator<Forum>() {
        // распаковываем объект из Parcel
        public Forum createFromParcel(Parcel in) {

            return new Forum(in);
        }

        public Forum[] newArray(int size) {
            return new Forum[size];
        }
    };

    private Forum(Parcel parcel) {
        m_Id = parcel.readString();
        m_Title = parcel.readString();
        description = parcel.readString();
        hasTopics = parcel.readByte() == 1;
        level = parcel.readInt();
        Boolean hasParent = parcel.readByte() == 1;
        if (hasParent)
            parent = new Forum(parcel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(m_Id);
        parcel.writeString(m_Title);
        parcel.writeString(description == null ? null : description.toString());
        parcel.writeByte((byte) (hasTopics ? 1 : 0));
        parcel.writeInt(level);
        if(parent==null){
            parcel.writeByte((byte) 0);
        }else{
            parcel.writeByte((byte) 1);
            ((Forum)parent).writeToParcel(parcel,i);
        }
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentId() {
        return parentId;
    }
}
