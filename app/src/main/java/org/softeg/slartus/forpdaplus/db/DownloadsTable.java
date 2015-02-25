package org.softeg.slartus.forpdaplus.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.DownloadTask;
import org.softeg.slartus.forpdaplus.classes.common.Functions;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 19.10.12
 * Time: 22:11
 * To change this template use File | Settings | File Templates.
 */
public class DownloadsTable {

    public static final String TABLE_NAME = "Downloads";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_CREATEDATETIME = "CreateDatetime";
    public static final String COLUMN_ENDDATETIME = "EndDatetime";
    public static final String COLUMN_FILEPATH = "FilePath";
    public static final String COLUMN_DOWNLOADFILEPATH = "DownloadFilePath";
    public static final String COLUMN_CONTENTLEGTH = "ContentLength";
    public static final String COLUMN_STATE = "State";
    public static final String COLUMN_DOWNLOADEDCONTENTLENGTH = "DownloadedContentLength";


    public static void insertRow(int id, String url, Date createDateTime) {
        SQLiteDatabase db = null;

        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();
            // db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, id);
            values.put(COLUMN_URL, url);
            values.put(COLUMN_CREATEDATETIME, DbHelper.DateTimeFormat.format(createDateTime));
            values.put(COLUMN_ENDDATETIME, DbHelper.DateTimeFormat.format(createDateTime));

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

    public static void updateRow(int notificationId, String downloadingFilePath, String outputFile, long contentLength) {
        SQLiteDatabase db = null;

        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();
            // db.beginTransaction();
            ContentValues cv = new ContentValues();

            cv.put(COLUMN_DOWNLOADFILEPATH, downloadingFilePath);
            cv.put(COLUMN_FILEPATH, outputFile);
            cv.put(COLUMN_CONTENTLEGTH, contentLength + "");
            db.update(TABLE_NAME, cv, COLUMN_ID + "=" + notificationId, null);
        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
        } finally {
            if (db != null) {
                //  db.endTransaction();
                db.close();
            }
        }
    }

    private static int NEXT_ID = -1;

    public static int getNextId() {
        SQLiteDatabase db = null;
        Cursor mcursor = null;
        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();
            String count = "SELECT max(" + COLUMN_ID + ") FROM " + DownloadsTable.TABLE_NAME;
            mcursor = db.rawQuery(count, null);
            mcursor.moveToFirst();
            int nextId = mcursor.getInt(0) + 1;
            NEXT_ID= Math.max(NEXT_ID + 1, nextId);
            return NEXT_ID;
        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
            return Functions.getUniqueDateInt();
        } finally {
            if (mcursor != null)
                mcursor.close();
            if (db != null) {
                //   db.endTransaction();
                db.close();
            }
        }

    }


    public static void endRow(DownloadTask downloadTask) {
        SQLiteDatabase db = null;

        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();
            //  db.beginTransaction();
            ContentValues cv = new ContentValues();

            cv.put(COLUMN_ENDDATETIME, DbHelper.DateTimeFormat.format(downloadTask.getStateChangedDate()));
            cv.put(COLUMN_STATE, downloadTask.getState());
            cv.put(COLUMN_DOWNLOADEDCONTENTLENGTH, downloadTask.getDownloadedSize());

            db.update(TABLE_NAME, cv, COLUMN_ID + "=" + downloadTask.getId(), null);
        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
        } finally {
            if (db != null) {
                //   db.endTransaction();
                db.close();
            }
        }
    }

}
