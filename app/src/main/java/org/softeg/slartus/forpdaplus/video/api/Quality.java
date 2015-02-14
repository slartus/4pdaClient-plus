package org.softeg.slartus.forpdaplus.video.api;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Format in the "fmt_list" parameter
 */
public class Quality implements Parcelable,Serializable {

    private CharSequence height;
    private CharSequence fileName;
    private boolean hd = false;

    public Quality() {

    }

    public CharSequence getHeight() {
        return height;
    }

    public void setHeight(CharSequence value) {
        height = value;
    }

    public CharSequence getFileName() {
        return fileName;
    }

    public void setFileName(CharSequence value) {
        this.fileName = value;
    }

    public static final Creator<Quality> CREATOR
            = new Creator<Quality>() {
        public Quality createFromParcel(Parcel in) {
            return new Quality(in);
        }

        public Quality[] newArray(int size) {
            return new Quality[size];
        }
    };

    protected Quality(Parcel parcel) {
        height = parcel.readString();
        fileName = parcel.readString();
        hd = parcel.readByte() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(VideoItem.getValueOrEmpty(height));
        parcel.writeString(VideoItem.getValueOrEmpty(fileName));
        parcel.writeByte(hd ? (byte) 1 : (byte) 0);
    }


    public CharSequence getTitle() {
        return height + (hd ? " (HD)" : "");
    }
}
