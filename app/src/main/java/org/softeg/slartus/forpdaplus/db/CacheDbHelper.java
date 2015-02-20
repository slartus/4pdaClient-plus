package org.softeg.slartus.forpdaplus.db;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.IOException;

/**
 * Created by slinkin on 19.02.14.
 */
public class CacheDbHelper extends SQLiteAssetHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "cache";

    public CacheDbHelper(Context context) throws IOException {
        super(context, DATABASE_NAME, Preferences.System.getSystemDir(), null, DATABASE_VERSION);
    }

    private static final Object m_Lock = new Object();


}
