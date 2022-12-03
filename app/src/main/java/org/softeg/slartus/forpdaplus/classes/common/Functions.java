package org.softeg.slartus.forpdaplus.classes.common;

import java.util.Date;

public class Functions {
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
}
