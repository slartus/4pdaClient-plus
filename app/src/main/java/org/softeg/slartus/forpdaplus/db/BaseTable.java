package org.softeg.slartus.forpdaplus.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BaseTable {

    public static int getRowsCount(SQLiteDatabase db, String tableName) {
        Cursor mcursor = null;
        try {
            String query = "SELECT count(*) FROM " + tableName;
            mcursor = db.rawQuery(query, null);

            mcursor.moveToFirst();
            return mcursor.getInt(0);
        } finally {
            if (mcursor != null)
                mcursor.close();
        }
    }
}
