package org.softeg.slartus.forpdaplus.post;/*
 * Created by slinkin on 19.03.14.
 */

import android.os.Parcel;
import android.os.Parcelable;

public class Attach implements Parcelable {
    private String mId;
    private String mName;

    public Attach(String id, String name) {
        mId = id;
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return mName;
    }

    public static final Parcelable.Creator<Attach> CREATOR = new Parcelable.Creator<Attach>() {
        // распаковываем объект из Parcel
        public Attach createFromParcel(Parcel in) {

            return new Attach(in);
        }

        public Attach[] newArray(int size) {
            return new Attach[size];
        }
    };

    private Attach(Parcel parcel) {
        mId=parcel.readString();
        mName=parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mName);
    }
}
