package org.softeg.slartus.forpdacommon;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by slinkin on 26.08.13.
 */
public class Functions {
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    public static SimpleDateFormat parseDateTimeFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");

    public static Boolean isWebviewAllowJavascriptInterface() {

        final SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(FACTORY.getApplication().get());
        return prefs.getBoolean("system.WebviewAllowJavascriptInterface", true);
//        return !Build.VERSION.RELEASE.startsWith("2.3")|| Build.VERSION.RELEASE.equals("2.3.7")
//                || Build.VERSION.RELEASE.equals("2.3.4");
    }

    public static String getToday() {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        return dateFormat.format(nowCalendar.getTime());
    }

    public static String getYesterToday() {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        nowCalendar.add(Calendar.DAY_OF_MONTH, -1);
        return dateFormat.format(nowCalendar.getTime());
    }

    public static String getForumDateTime(Date date) {

        if (date == null) return "";
        return parseDateTimeFormat.format(date);
    }

    public static String getNewsDateTime(Date date) {

        if (date == null) return "";
        return dateFormat.format(date);
    }

    public static Date parseForumDateTime(String dateTime, String today, String yesterday) {
        try {
            Date res = parseDateTimeFormat.parse(dateTime.replace("Сегодня", today).replace("Вчера", yesterday));

            Calendar calendar = new GregorianCalendar();
            calendar.setTime(res);
            int year = calendar.get(Calendar.YEAR);
            if (year < 100)
                calendar.set(Calendar.YEAR, 2000 + year);
            return calendar.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date parseForumDateTime(String dateTime) {
        return parseForumDateTime(dateTime, getToday(), getYesterToday());
    }
}
