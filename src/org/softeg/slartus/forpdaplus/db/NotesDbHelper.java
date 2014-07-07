package org.softeg.slartus.forpdaplus.db;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.softeg.slartus.forpdaplus.MyApp;

import java.io.IOException;

/**
 * Created by slinkin on 07.08.13.
 */
public class NotesDbHelper extends SQLiteAssetHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "notes";

    public NotesDbHelper(Context context) throws IOException {
        super(context, DATABASE_NAME, MyApp.getInstance().getAppExternalFolderPath(), null, DATABASE_VERSION);

    }

}
