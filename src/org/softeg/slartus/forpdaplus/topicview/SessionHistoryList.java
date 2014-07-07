package org.softeg.slartus.forpdaplus.topicview;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 05.03.13
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */
public class SessionHistoryList extends ArrayList<SessionHistory> implements Parcelable {

    public SessionHistoryList() {

    }

    public SessionHistoryList(Parcel in) {
        this();
        readFromParcel(in);
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SessionHistoryList createFromParcel(Parcel in) {
            return new SessionHistoryList(in);
        }

        public Object[] newArray(int arg0) {
            return null;
        }
    };

    private void readFromParcel(Parcel in) {
        this.clear();
        m_First = in.readInt() == 1;
        // read the list size
        int size = in.readInt();

        // order of the in.readString is fundamental
        // it must be ordered as it is in the Part.java file

        for (int i = 0; i < size; i++) {
            SessionHistory sessionHistory = new SessionHistory();
            sessionHistory.readFromParcel(in);

            this.add(sessionHistory);
        }
    }

    private Boolean m_First = true;

    public void addSessionHistory(String themeId, String params, int x, int y) {
        if (m_First) {
            m_First = false;
            return;
        }
        if (size() != 0) {
            SessionHistory prevHistory = get(size() - 1);
            if (prevHistory.getUrl().equals(SessionHistory.createUrl(themeId, SessionHistory.getSt(params))))
                return;
        }
        add(new SessionHistory(themeId, params, x, y));
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeInt(m_First ? 1 : 0);
        int size = this.size();

        arg0.writeInt(size);

        for (int i = 0; i < size; i++) {
            SessionHistory p = this.get(i);
            p.writeToParcel(arg0, arg1);
        }
    }
}