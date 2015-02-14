package org.softeg.slartus.forpdaplus.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

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
