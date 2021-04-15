package org.softeg.slartus.forpdaplus.classes.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.softeg.slartus.forpdaplus.App;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 29.09.11
 * Time: 20:37
 * To change this template use File | Settings | File Templates.
 */
public class Functions {


    private static final SimpleDateFormat fullDateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


    public static Boolean isWebviewAllowJavascriptInterface() {
        return org.softeg.slartus.forpdacommon.Functions.isWebviewAllowJavascriptInterface();
    }


    public static String getToday() {

        return org.softeg.slartus.forpdacommon.Functions.getToday();
    }

    public static String getYesterToday() {

        return org.softeg.slartus.forpdacommon.Functions.getYesterToday();
    }

    public static String getForumDateTime(Date date) {
        return org.softeg.slartus.forpdacommon.Functions.getForumDateTime(date);

    }

    public static Date parseForumDateTime(String dateTime) {
        return org.softeg.slartus.forpdacommon.Functions.parseForumDateTime(dateTime);

    }

    public static Date parseForumDateTime(String dateTime, String today, String yesterday) {
        return org.softeg.slartus.forpdacommon.Functions.parseForumDateTime(dateTime, today, yesterday);
    }

    public static String getFullDateString(Date date) {

        if (date == null) return "";
        return fullDateTimeFormat.format(date);
    }

    public static Date getFullDate(String dateString, Date defaultValue) {

        if (TextUtils.isEmpty(dateString)) return defaultValue;
        try {
            return fullDateTimeFormat.parse(dateString);
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    public static Boolean isImageUrl(String url) {
        return Pattern.compile("(png|jpeg|jpg)$").matcher(url).find();
    }

    public static String getSizeText(long bytes) {
        if (bytes < 1000)
            return bytes + "Б";
        if (bytes < 1000 * 1024)
            return String.format("%.1fКб", (float) bytes / 1024);
        if (bytes < 1000 * 1024 * 1024)
            return String.format("%.1fМб", (float) bytes / 1024 / 1024);

        return String.format("%.1fГб", (float) bytes / 1024 / 1024 / 1024);
    }

    public static int getUniqueDateInt() {
        Calendar calendar = new GregorianCalendar();
        // максимум: 2147483647
        // 1д22ч.74м.83с.647мс
        //ччммссмммм
//        calendar.getTimeInMillis()
        int res = calendar.get(Calendar.HOUR_OF_DAY) * 10000000 +
                calendar.get(Calendar.MINUTE) * 100000 +
                calendar.get(Calendar.SECOND) * 1000 +
                calendar.get(Calendar.MILLISECOND);

        return res;
    }

}
