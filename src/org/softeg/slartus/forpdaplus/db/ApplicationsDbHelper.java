package org.softeg.slartus.forpdaplus.db;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.softeg.slartus.forpdaplus.MyApp;

import java.io.IOException;

/**
 * Created by slinkin on 16.01.14.
 */
public class ApplicationsDbHelper extends SQLiteAssetHelper {

    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "applications";

    public ApplicationsDbHelper(Context context) throws IOException {
        super(context, DATABASE_NAME, MyApp.getInstance().getAppExternalFolderPath(), null, DATABASE_VERSION);
        setForcedUpgrade(8);
    }
}
