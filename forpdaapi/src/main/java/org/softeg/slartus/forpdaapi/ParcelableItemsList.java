package org.softeg.slartus.forpdaapi;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by slinkin on 17.01.14.
 */
public abstract class ParcelableItemsList extends ArrayList<IListItem> implements Parcelable {

    public abstract IListItem createNewListItem();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public static final Parcelable.Creator<IListItem> CREATOR = new Parcelable.Creator<IListItem>() {
        public IListItem createFromParcel(Parcel s) {
            return null;

            // return new createNewListItem(s);
        }

        public IListItem[] newArray(int size) {
            return new IListItem[size];
        }
    };
}

