package org.softeg.slartus.forpdacommon;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: slinkin
 * Date: 03.10.11
 * Time: 11:05
 */
public class ExtPreferences {

    private static final SimpleDateFormat s_DateTimeFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    public static Date getDateTime(SharedPreferences prefs, String key, Date defValue) {
        try {
            String res = prefs.getString(key, defValue == null ? null : s_DateTimeFormat.format(defValue));
            if (TextUtils.isEmpty(res)) return defValue;

            return s_DateTimeFormat.parse(res);
        } catch (Throwable ex) {

            return defValue;
        }
    }

    public static SharedPreferences.Editor putDateTime(SharedPreferences.Editor editor, String key, Date value) {
        editor.putString(key, s_DateTimeFormat.format(value));
        return editor;
    }

    public static float parseFloat(SharedPreferences prefs, String key, float defValue) {
        try {
            String res = prefs.getString(key, Float.toString(defValue));
            if (TextUtils.isEmpty(res)) return defValue;

            return Float.parseFloat(res);
        } catch (Throwable ex) {
            try {
                return prefs.getFloat(key, defValue);
            } catch (Throwable ex1) {

            }

        }
        return defValue;
    }

    public static int parseInt(SharedPreferences prefs, String key, int defValue) {
        try {
            String res = prefs.getString(key, Integer.toString(defValue));
            if (TextUtils.isEmpty(res)) return defValue;

            return Integer.parseInt(res);
        } catch (Throwable ex) {
            try {
                return prefs.getInt(key, defValue);
            } catch (Throwable ex1) {

            }

        }
        return defValue;

    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, defaultValue);
    }
}
