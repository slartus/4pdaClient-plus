package org.softeg.slartus.forpdaplus.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.softeg.slartus.forpdaapi.Forum;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.classes.forum.HistoryTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.repositories.ForumsRepository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class TopicsHistoryTable {

    public static final String TABLE_NAME = "TopicsHistory";

    public static final String COLUMN_TOPIC_ID = "Topic_id";
    public static final String COLUMN_DATETIME = "DateTime";
    public static final String COLUMN_URL = "Url";

    public static String getTopicHistoryUrl(CharSequence topicId) {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getReadableDatabase();
            assert db != null;

            c = db.query("TopicsHistoryView", new String[]{COLUMN_URL}, TopicsTable.COLUMN_ID + "=?", new String[]{topicId.toString()},
                    null, null, null);


            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex(COLUMN_URL));
            }

        } finally {
            if (db != null) {
                if (c != null)
                    c.close();
                db.close();
            }
        }
        return null;
    }

    public static ArrayList<HistoryTopic> getTopicsHistory(ListInfo listInfo) {

        ArrayList<HistoryTopic> res = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getReadableDatabase();


            listInfo.setOutCount(BaseTable.getRowsCount(db, "TopicsHistoryView"));

            c = db.query("TopicsHistoryView", null, null, null, null, null, null, listInfo.getFrom() + ", 20");

            if (c.moveToFirst()) {
                int columnIdIndex = c.getColumnIndex(TopicsTable.COLUMN_ID);
                int columnTitleIndex = c.getColumnIndex(TopicsTable.COLUMN_TITLE);
                int columnDescriptionIndex = c.getColumnIndex(TopicsTable.COLUMN_DESCRIPTION);
                int columnForumIdIndex = c.getColumnIndex(TopicsTable.COLUMN_FORUM_ID);
                int columnDateTimeIndex = c.getColumnIndex(COLUMN_DATETIME);
                int columnUrlIndex = c.getColumnIndex(COLUMN_URL);
                //  int columnForumTitleIndex = c.getColumnIndex("ForumTitle");
                do {
                    String id = c.getString(columnIdIndex);
                    String title = c.getString(columnTitleIndex);
                    String description = c.getString(columnDescriptionIndex);
                    String forumId = c.getString(columnForumIdIndex);
                    String forumTitle = null;
                    String url = c.getString(columnUrlIndex);
                    Forum f = ForumsRepository.getInstance().findById(forumId);
                    if (f != null)
                        forumTitle = f.getTitle();
                    Date dateTime = null;
                    try {
                        dateTime = DbHelper.parseDate(c.getString(columnDateTimeIndex));
                    } catch (ParseException e) {
                        AppLog.e(App.getContext(), e);
                    }

                    HistoryTopic topic = new HistoryTopic(id, title, url);
                    topic.setDescription(description);
                    topic.setForumId(forumId);
                    topic.setForumTitle(forumTitle);
                    topic.setLastMessageDate(dateTime);
                    res.add(topic);
                } while (c.moveToNext());

            }

        } finally {
            if (db != null) {
                if (c != null)
                    c.close();
                db.close();
            }
        }
        return res;
    }

    public static void addHistory(ExtTopic topic, String lastUrl) {
        if (topic.getId() == null) return;
        TopicsTable.addTopic(topic, true);
        SQLiteDatabase db = null;

        try {

            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();

            assert db != null;
            db.execSQL("delete from " + TABLE_NAME + " where " + COLUMN_TOPIC_ID + "=" + topic.getId());

            ContentValues values = new ContentValues();
            values.put(COLUMN_TOPIC_ID, topic.getId());
            values.put(COLUMN_DATETIME, DbHelper.DateTimeFormat.format(new Date()));
            values.put(COLUMN_URL, lastUrl);
            db.insertOrThrow(TABLE_NAME, null, values);
        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
        } finally {
            if (db != null) {

                db.close();
            }
        }
    }

    public static void delete(CharSequence id) {
        SQLiteDatabase db = null;

        try {

            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();

            assert db != null;
            db.execSQL("delete from " + TABLE_NAME + " where " + COLUMN_TOPIC_ID + "=" + id);

        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
        } finally {
            if (db != null) {

                db.close();
            }
        }
    }
}
