package org.softeg.slartus.forpdaplus.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.classes.Forum;
import org.softeg.slartus.forpdaplus.classes.Themes;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by slinkin on 06.08.13.
 */
public class TabTable {
    public static final String TABLE_NAME = "TabTopics";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NUMBER = "Number";
    public static final String COLUMN_TEMPLATE = "Template";
    public static final String COLUMN_MAXCOUNT = "MaxCount";

    //    public static final String COLUMN_ID = "_id";
//    public static final String COLUMN_FORUM_ID = "ForumId";
//    public static final String COLUMN_TITLE = "Title";
//    public static final String COLUMN_DESCRIPTION = "Description";
//    public static final String COLUMN_LAST_MESSAGE_DATE = "LastMessageDate";
//    public static final String COLUMN_LAST_MESSAGE_Author = "LastMessageAuthor";
//    public static final String COLUMN_HASNEW = "HasNew";
//    public static final String COLUMN_NUMBER = "Number";
    public static Themes loadTab(String template) {
        SQLiteDatabase db = null;
        Cursor c = null;
        Themes res = new Themes();
        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getReadableDatabase();

            String[] selectionArgs = new String[]{template, "-1"};

            c = db.rawQuery("select MaxCount from TabTopics where Template=? and _id=?", selectionArgs);
            if (c.moveToFirst())
                res.setThemesCountInt(c.getInt(0));
            else
                return res;
            selectionArgs = new String[]{template};
            c = db.rawQuery("select t.* from Topics t inner join TabTopics tt on tt._id=t._id where Template=?", selectionArgs);
            Forum forum = ForumsTableOld.loadForumsTree();
            if (c.moveToFirst()) {
                int columnIdIndex = c.getColumnIndex(TopicsTable.COLUMN_ID);
                int columnTitleIndex = c.getColumnIndex(TopicsTable.COLUMN_TITLE);
                int columnDescriptionIndex = c.getColumnIndex(TopicsTable.COLUMN_DESCRIPTION);
                int columnForumIdIndex = c.getColumnIndex(TopicsTable.COLUMN_FORUM_ID);
                int columnDateTimeIndex = c.getColumnIndex(TopicsTable.COLUMN_LAST_MESSAGE_DATE);
                int columnAuthorIndex = c.getColumnIndex(TopicsTable.COLUMN_LAST_MESSAGE_Author);
                int columnHasNewIndex = c.getColumnIndex(TopicsTable.COLUMN_HASNEW);
                //  int columnForumTitleIndex = c.getColumnIndex("ForumTitle");
                do {
                    String id = c.getString(columnIdIndex);

                    String title = c.getString(columnTitleIndex);
                    String description = c.getString(columnDescriptionIndex);
                    String forumId = c.getString(columnForumIdIndex);

                    String forumTitle = null;
                    Forum f = forum.findById(forumId, true, false);
                    if (f != null)
                        forumTitle = f.getTitle();
                    Date dateTime = null;
                    try {
                        dateTime = DbHelper.parseDate(c.getString(columnDateTimeIndex));
                    } catch (ParseException e) {
                        AppLog.e(App.getContext(), e);
                    }
                    String lastMessageAuthor = c.getString(columnAuthorIndex);
                    Boolean hasNew = c.getInt(columnHasNewIndex) == 1;

                    ExtTopic topic = new ExtTopic(id, title);
                    topic.setDescription(description);
                    topic.setForumId(forumId);
                    topic.setForumTitle(forumTitle);
                    topic.setLastMessageDate(dateTime);
                    topic.setLastMessageAuthor(lastMessageAuthor);
                    topic.setIsNew(hasNew);
                    res.add(topic);
                } while (c.moveToNext());

            }
        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
        } finally {
            if (db != null) {
                if (c != null)
                    c.close();
                db.close();
            }
        }
        return res;
    }

    public static void saveTab(Themes topics, String template, int count) {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();
            db.execSQL("delete from " + TABLE_NAME + " where Template='" + template + "'");
            saveTabInfo(db, topics, template);
            for (int i = 0; i < Math.min(count, topics.size()); i++) {
                saveTabTable(db, topics.get(i), template);
            }

        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
        } finally {
            if (db != null) {
                if (c != null)
                    c.close();
                db.close();
            }
        }
    }

    private static void saveTabTable(SQLiteDatabase db, ExtTopic topic, String template) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, topic.getId());
        values.put(COLUMN_TEMPLATE, template);
        db.insertOrThrow(TABLE_NAME, null, values);
        TopicsTable.addTopic(db, topic, false);
    }

    private static void saveTabInfo(SQLiteDatabase db, Themes topics, String template) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, -1);
        values.put(COLUMN_TEMPLATE, template);
        values.put(COLUMN_MAXCOUNT, topics.getThemesCount());
        db.insertOrThrow(TABLE_NAME, null, values);

    }
}
