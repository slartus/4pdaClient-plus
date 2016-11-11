package org.softeg.slartus.forpdaplus.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.PdaApplication;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by slinkin on 16.01.14.
 */
public class ApplicationRelationsTable {
    public static final String TABLE_NAME = "ApplicationRelations";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_APP_URL = "app_url";
    public static final String COLUMN_PACK_NAME = "pack_name";
    public static final String COLUMN_BYUSER_NAME = "byuser";

    public static void addRealtion(CharSequence packName, CharSequence topicId) {
        SQLiteDatabase db = null;

        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());

            db = dbHelper.getWritableDatabase();
            String[] selectionArgs = new String[]{packName + "%"};
            db.execSQL("delete from " + TABLE_NAME + " where pack_name like ?", selectionArgs);

            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, UUID.randomUUID().toString());
            values.put(COLUMN_PACK_NAME, packName.toString());
            values.put(COLUMN_APP_URL, topicId.toString());
            values.put(COLUMN_BYUSER_NAME, true);


            db.insertOrThrow(TABLE_NAME, null, values);

        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
        } finally {
            if (db != null) {
                // db.endTransaction();
                db.close();
            }
        }
    }

    public static void addCacheRelation(SQLiteDatabase db, CharSequence packName, CharSequence topicId) {


        try {


            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, UUID.randomUUID().toString());
            values.put(COLUMN_PACK_NAME, packName.toString());
            values.put(COLUMN_APP_URL, topicId.toString());


            db.update(TABLE_NAME, values, null, null);

        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
        }
    }


    public static ArrayList<PdaApplication> getApplications(SQLiteDatabase db, CharSequence packName) throws IOException {
        ArrayList<PdaApplication> res = new ArrayList<PdaApplication>();

        Cursor c = null;

        try {
            String[] selectionArgs = new String[]{packName + "%"};
            c = db.rawQuery("select app_url,pack_name from ApplicationRelations where pack_name like ?", selectionArgs);
            if (c.moveToFirst()) {
                int columnAppUrlIndex = c.getColumnIndex(COLUMN_APP_URL);
                int columnPackNameIndex = c.getColumnIndex(COLUMN_PACK_NAME);


                do {
                    String pckn = c.getString(columnPackNameIndex);
                    int appUrl = c.getInt(columnAppUrlIndex);

                    PdaApplication app = new PdaApplication();

                    app.AppUrl = appUrl;
                    app.PackName = pckn;
                    res.add(app);

                } while (c.moveToNext());
            }

        } finally {

            if (c != null)
                c.close();

        }

        return res;
    }


}
