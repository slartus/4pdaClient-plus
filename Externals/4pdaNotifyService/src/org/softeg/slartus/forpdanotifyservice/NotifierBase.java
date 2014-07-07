package org.softeg.slartus.forpdanotifyservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.softeg.slartus.forpdacommon.ExtDateFormat;
import org.softeg.slartus.forpdacommon.ExtPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by slinkin on 05.06.13.
 */
public abstract class NotifierBase {
    private Context mContext;

    public NotifierBase(Context context) {
        mContext = context;
    }

    public Context getContext(){
        return mContext;
    }

    public abstract void readSettings(Context context, Intent intent);
    public abstract void restartTask(Context context);
    public abstract void cancel(Context context);
    public abstract void checkUpdates();

    protected String loadCookiesPath() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getString("CookiesPath", null);
    }

    private SimpleDateFormat m_DateTimeFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    protected GregorianCalendar loadLastDate(String lastDateTimeKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String lastDateTimeStr = preferences.getString(lastDateTimeKey, null);
        // Log.d(LOG_TAG, "lastDateTimeStr=" + lastDateTimeStr);
        if (lastDateTimeStr == null) return null;

        Map<String, Date> additionalHeaders = new HashMap<String, Date>();

        if (ExtDateFormat.tryParse(m_DateTimeFormat, lastDateTimeStr, additionalHeaders)) {

            GregorianCalendar lastDateTime = new GregorianCalendar();
            lastDateTime.setTime(additionalHeaders.get("date"));
            // Log.d(LOG_TAG, "loadLastDate=" + additionalHeaders.get("date"));
            return lastDateTime;
        }
        return null;
    }

    protected void saveLastDate(GregorianCalendar calendar, String lastDateTimeKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(lastDateTimeKey, m_DateTimeFormat.format(calendar.getTime()));
        editor.commit();
    }

    protected static void saveTimeOut(Context context, float timeOut, String timeOutKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(timeOutKey, timeOut);
        editor.commit();
    }

    protected static float loadTimeOut(Context context, String timeOutKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return ExtPreferences.parseFloat(preferences, timeOutKey, 5);
    }

}
