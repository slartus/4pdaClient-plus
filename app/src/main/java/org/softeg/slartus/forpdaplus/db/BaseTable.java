package org.softeg.slartus.forpdaplus.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 22.11.12
 * Time: 10:59
 * To change this template use File | Settings | File Templates.
 */
public class BaseTable {

    public static String getDateString(Date date) {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(date);
    }

    public static Date parseDate(String val) throws ParseException {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(val);
    }

    public static int getRowsCount(SQLiteDatabase db, String tableName) {
        Cursor mcursor = null;
        try {
            String query = "SELECT count(*) FROM " + tableName;
            mcursor = db.rawQuery(query, null);

            mcursor.moveToFirst();
            return mcursor.getInt(0);
        } finally {
            mcursor.close();
        }
    }
}
