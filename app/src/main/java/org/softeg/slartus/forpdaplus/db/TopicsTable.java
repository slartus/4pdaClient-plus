package org.softeg.slartus.forpdaplus.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;

public class TopicsTable {
    public static final String TABLE_NAME = "Topics";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FORUM_ID = "ForumId";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String COLUMN_LAST_MESSAGE_DATE = "LastMessageDate";
    public static final String COLUMN_LAST_MESSAGE_Author = "LastMessageAuthor";
    public static final String COLUMN_HASNEW = "HasNew";

    public static void addTopic(ExtTopic topic, Boolean ifNotExists) {
        SQLiteDatabase db = null;

        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();

            addTopic(db, topic, ifNotExists);
        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static void addTopic(SQLiteDatabase db, ExtTopic topic, Boolean ifNotExists) {
        String count = "SELECT count(*) FROM " + TABLE_NAME + " where _id=?";
        String[] args = new String[]{topic.getId()};
        Cursor mcursor = db.rawQuery(count, args);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        mcursor.close();
        if (icount > 0 && ifNotExists) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, topic.getId());
        values.put(COLUMN_FORUM_ID, topic.getForumId());
        values.put(COLUMN_TITLE, topic.getTitle());
        values.put(COLUMN_DESCRIPTION, topic.getDescription());
        if (topic.getLastMessageDate() != null)
            values.put(COLUMN_LAST_MESSAGE_DATE, DbHelper.DateTimeFormat.format(topic.getLastMessageDate()));
        if (topic.getLastMessageAuthor() != null)
            values.put(COLUMN_LAST_MESSAGE_Author, topic.getLastMessageAuthor());
        values.put(COLUMN_HASNEW, topic.getIsNew());
        if (icount > 0) {
            db.update(TABLE_NAME, values, "_id=?", new String[]{topic.getId()});
        } else {
            db.insertOrThrow(TABLE_NAME, null, values);
        }

    }
}
