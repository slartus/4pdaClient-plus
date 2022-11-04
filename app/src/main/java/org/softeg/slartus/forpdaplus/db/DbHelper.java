package org.softeg.slartus.forpdaplus.db;

import android.content.Context;
import android.text.TextUtils;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DbHelper extends SQLiteAssetHelper {
    public static SimpleDateFormat DateTimeFormat = new SimpleDateFormat(
            "yyyy.MM.dd HH:mm:ss", Locale.ENGLISH);
    private static final int DATABASE_VERSION = 11;
    private static final String DATABASE_NAME = "base";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, Preferences.System.getSystemDir(), null, DATABASE_VERSION);
        setForcedUpgrade(9);
    }

    private DbHelper(Context context, @SuppressWarnings("unused") Boolean withoutUpgrade) {
        super(context, DATABASE_NAME, Preferences.System.getSystemDir(), null, 1);
    }

    public static void prepareBases(Context context) throws IOException {
        new DbHelper(context, true);
    }

    public static Date parseDateOrNull(String text) {
        if (text == null || TextUtils.isEmpty(text))
            return null;
        try {
            return DateTimeFormat.parse(text.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getDateString(Date date) {

        return DateTimeFormat.format(date);
    }
}
