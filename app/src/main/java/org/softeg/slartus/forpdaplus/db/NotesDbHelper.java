package org.softeg.slartus.forpdaplus.db;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import ru.slartus.forpda.feature_preferences.Preferences;

import java.io.IOException;

/**
 * Created by slinkin on 07.08.13.
 */
public class NotesDbHelper extends SQLiteAssetHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "notes";
    public static final String DATABASE_DIR =  Preferences.System.getSystemDir();

    public NotesDbHelper(Context context) throws IOException {
        super(context, DATABASE_NAME, DATABASE_DIR, null, DATABASE_VERSION);

    }

}
