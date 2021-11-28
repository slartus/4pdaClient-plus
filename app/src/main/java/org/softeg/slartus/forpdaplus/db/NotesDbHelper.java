package org.softeg.slartus.forpdaplus.db;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.IOException;

public class NotesDbHelper extends SQLiteAssetHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "notes";
    public static final String DATABASE_DIR =  Preferences.System.getSystemDir();

    public NotesDbHelper(Context context) throws IOException {
        super(context, DATABASE_NAME, DATABASE_DIR, null, DATABASE_VERSION);

    }

}
