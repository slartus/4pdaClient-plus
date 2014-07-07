package org.softeg.slartus.forpdaplus.topicview;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 05.03.13
 * Time: 13:38
 * To change this template use File | Settings | File Templates.
 */
public class SessionHistory implements Parcelable {
    public String m_ThemeId;
    public String m_St;
    private int m_X;
    private int m_Y;

    public SessionHistory() {
        super();
    }

    public SessionHistory(String themeId, String params, int x, int y) {
        super();
        m_ThemeId = themeId;
        m_St = getSt(params);
        m_X = x;
        m_Y = y;
    }

    public final Parcelable.Creator<SessionHistory> CREATOR = new Parcelable.Creator<SessionHistory>() {
        public SessionHistory createFromParcel(Parcel in) {
            return new SessionHistory(in);
        }

        public SessionHistory[] newArray(int size) {
            return new SessionHistory[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


    public SessionHistory(Parcel in) {
        this();
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.m_ThemeId = in.readString();
        this.m_St = in.readString();
        this.m_X = in.readInt();
        this.m_Y = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(m_ThemeId);
        parcel.writeString(m_St);

        parcel.writeInt(m_X);
        parcel.writeInt(m_Y);
    }

    public int getX() {
        return m_X;
    }

    public int getY() {
        return m_Y;
    }

    public static String getSt(String params) {
        if (TextUtils.isEmpty(params)) return null;

        Matcher m = Pattern.compile("st=(\\d+)").matcher(params);
        if (m.find())
            return "st=" + m.group(1);
        return null;
    }

    public static String createUrl(String themeId, String st) {
        return "showtopic=" + themeId + (TextUtils.isEmpty(st) ? "" : ("&" + st));
    }

    public String getUrl() {
        return createUrl(m_ThemeId, m_St);
    }
}
