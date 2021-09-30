package org.softeg.slartus.forpdaplus.db;

import android.content.Context;
import android.text.TextUtils;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.softeg.slartus.forpdaplus.feature_preferences.Preferences;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 19.10.12
 * Time: 22:08
 * To change this template use File | Settings | File Templates.
 */
public class DbHelper extends SQLiteAssetHelper {
    public static SimpleDateFormat DateTimeFormat = new SimpleDateFormat(
            "yyyy.MM.dd HH:mm:ss");
    private static final int DATABASE_VERSION = 11;
    private static final String DATABASE_NAME = "base";

    public DbHelper(Context context) throws IOException {
        super(context, DATABASE_NAME, Preferences.System.getSystemDir(), null, DATABASE_VERSION);
        setForcedUpgrade(9);
    }

    private DbHelper(Context context, Boolean whithoutUpgrade) throws IOException {
        super(context, DATABASE_NAME, Preferences.System.getSystemDir(), null, 1);
    }

    public static void prepareBases(Context context) throws IOException {
        DbHelper helper = new DbHelper(context, true);
        helper.MigrateNotesTable();
    }

    private void MigrateNotesTable() {
//        SQLiteDatabase db = null;
//        try {
//            int baseVersion = this.getDatabaseVersion();
//
//            if (baseVersion >= 8 || baseVersion == -1) return;
//            ArrayList<Note> notes = NotesTable.getNotes(this, null);
//            NotesDbHelper dbHelper = new NotesDbHelper(MyApp.getInstance());
//            for (Note note : notes) {
//                NotesTable.insertNote(dbHelper, note);
//            }
//            NotesTable.deleteAll(this);
//
//        } catch (Throwable e) {
//            e.printStackTrace();
//        } finally {
//            if (db != null) {
//
//                db.close();
//            }
//        }
    }

    public static Date parseDate(String text) throws ParseException {
        if (TextUtils.isEmpty(text))
            return null;
        return DateTimeFormat.parse(text);
    }

    public static String getDateString(Date date) throws ParseException {

        return DateTimeFormat.format(date);
    }
}
