package org.softeg.slartus.forpdanotifyservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import org.softeg.slartus.forpdaapi.ClientPreferences;
import org.softeg.slartus.forpdacommon.ExtDateFormat;
import org.softeg.slartus.forpdacommon.ExtPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
 * Created by slartus on 05.06.13.
 */
public abstract class NotifierBase {
    private final Context mContext;

    public NotifierBase(Context context) {
        mContext = context;
    }

    public Context getContext() {
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

    private final SimpleDateFormat m_DateTimeFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());

    protected GregorianCalendar loadLastDate(String lastDateTimeKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String lastDateTimeStr = preferences.getString(lastDateTimeKey, null);
        // Log.d(LOG_TAG, "lastDateTimeStr=" + lastDateTimeStr);
        if (lastDateTimeStr == null) return null;

        Map<String, Date> additionalHeaders = new HashMap<>();

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
        editor.apply();
    }

    protected static void saveTimeOut(Context context, float timeOut, String timeOutKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(timeOutKey, timeOut);
        editor.apply();
    }

    protected static float loadTimeOut(Context context, String timeOutKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return ExtPreferences.parseFloat(preferences, timeOutKey, 5);
    }

    protected static Uri getSound(Context context) {
        if (!ClientPreferences.Notifications.useSound(context))
            return null;
        if (ClientPreferences.Notifications.SilentMode.isEnabled(context)) {
            Calendar nowTime = Calendar.getInstance();
            Calendar startTime = ClientPreferences.Notifications.SilentMode.getStartTime(context);
            Calendar endTime = ClientPreferences.Notifications.SilentMode.getEndTime(context);
            if (endTime.before(startTime))
                endTime.add(Calendar.DAY_OF_YEAR, 1);
            if (nowTime.after(startTime) && nowTime.before(endTime))
                return null;
        }
        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (ClientPreferences.Notifications.isDefaultSound(context))
            return defaultUri;// Settings.System.DEFAULT_NOTIFICATION_URI

        return ClientPreferences.Notifications.getSound(context);
    }
}
