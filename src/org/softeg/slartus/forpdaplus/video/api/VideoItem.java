package org.softeg.slartus.forpdaplus.video.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class VideoItem implements Parcelable, Serializable {
    private CharSequence id;
    private CharSequence vId;
    private CharSequence previewImgUrl;
    private CharSequence channelUrl;
    private CharSequence dateString;
    private CharSequence channelTitle;
    private CharSequence title;
    private CharSequence url;

    private ArrayList<Quality> qualities = new ArrayList<>();
    private String defaultBitrate;



    public VideoItem() {

    }

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public CharSequence getId() {
        return id;
    }

    public void setId(CharSequence id) {
        this.id = id;
    }

    public CharSequence getUrl() {
        return url;
    }

    public void setUrl(CharSequence url) {
        this.url = url;
    }

    public static final Creator<VideoItem> CREATOR
            = new Creator<VideoItem>() {
        public VideoItem createFromParcel(Parcel in) {
            return new VideoItem(in);
        }

        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };

    protected VideoItem(Parcel parcel) {
        id = parcel.readString();
        vId = parcel.readString();

        previewImgUrl = parcel.readString();
        channelUrl = parcel.readString();
        dateString = parcel.readString();
        channelTitle = parcel.readString();
        title = parcel.readString();
        url = parcel.readString();
        defaultBitrate = parcel.readString();


        int arraysCount = parcel.readInt();
        for (int i = 0; i < arraysCount; i++) {
            qualities.add(new Quality(parcel));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static String getValueOrEmpty(CharSequence str) {
        if (str == null)
            return null;
        return str.toString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getValueOrEmpty(id));
        parcel.writeString(getValueOrEmpty(vId));

        parcel.writeString(getValueOrEmpty(previewImgUrl));
        parcel.writeString(getValueOrEmpty(channelUrl));
        parcel.writeString(getValueOrEmpty(dateString));
        parcel.writeString(getValueOrEmpty(channelTitle));
        parcel.writeString(getValueOrEmpty(title));
        parcel.writeString(getValueOrEmpty(url));
        parcel.writeString(getValueOrEmpty(defaultBitrate));


        parcel.writeInt(getQualities().size());
        for (Quality item : getQualities()) {
            item.writeToParcel(parcel, i);
        }
    }

    public CharSequence getDefaultVideoUrl() {
        return !TextUtils.isEmpty(defaultBitrate)?defaultBitrate: getQualities().get(0).getFileName();
    }

    public static String getFilePath(CharSequence fileName) {
        return fileName.toString();
    }


    public ArrayList<Quality> getQualities() {
        return qualities;
    }

    public CharSequence getvId() {
        return vId;
    }

    public void setDefaultBitrate(String defaultBitrate) {
        this.defaultBitrate = defaultBitrate;
    }

}
