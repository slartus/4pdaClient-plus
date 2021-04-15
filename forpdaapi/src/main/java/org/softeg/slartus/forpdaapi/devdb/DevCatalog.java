package org.softeg.slartus.forpdaapi.devdb;

import android.os.Parcel;
import android.os.Parcelable;

import org.softeg.slartus.forpdaapi.ICatalogItem;

/**
 * Created by slartus on 06.03.14.
 */
public class DevCatalog implements ICatalogItem, Parcelable {
    public static final int ROOT = -1;
    public static final int DEVICE_TYPE = 0;
    public static final int DEVICE_BRAND = 1;
    private final String mId;
    private final String mTitle;
    private String mImageUrl;
    private String description;
    private int type;
    private ICatalogItem parent;

    public DevCatalog(String id, String title) {
        mId = id;
        mTitle = title;
    }

    @Override
    public CharSequence getId() {
        return mId;
    }

    @Override
    public CharSequence getTitle() {
        return mTitle;
    }

    @Override
    public CharSequence getSubTitle() {
        return description;
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
        DevCatalog clone = new DevCatalog(mId, mTitle);
        clone.setType(type);
        clone.setDescription(description);
        clone.setImageUrl(mImageUrl);
        clone.setParent(parent == null ? null : parent.clone());
        return clone;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }


    public DevCatalog setType(int type) {
        this.type = type;
        return this;
    }

    public static final Parcelable.Creator<DevCatalog> CREATOR = new Parcelable.Creator<DevCatalog>() {
        // распаковываем объект из Parcel
        public DevCatalog createFromParcel(Parcel in) {

            return new DevCatalog(in);
        }

        public DevCatalog[] newArray(int size) {
            return new DevCatalog[size];
        }
    };

    private DevCatalog(Parcel parcel) {
        mId = parcel.readString();
        mTitle = parcel.readString();
        description = parcel.readString();
        mImageUrl = parcel.readString();
        type = parcel.readInt();
        Boolean hasParent = parcel.readByte() == 1;
        if (hasParent)
            parent = new DevCatalog(parcel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mTitle);
        parcel.writeString(description);
        parcel.writeString(mImageUrl);
        parcel.writeInt(type);
        if (parent == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            ((DevCatalog) parent).writeToParcel(parcel, i);
        }
    }
}
