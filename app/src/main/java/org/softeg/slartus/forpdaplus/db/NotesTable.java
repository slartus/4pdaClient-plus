package org.softeg.slartus.forpdaplus.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.notes.Note;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 20.02.13
 * Time: 11:21
 * To change this template use File | Settings | File Templates.
 */
public class NotesTable {
    public static final String TABLE_NAME = "Notes";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_BODY = "Body";
    public static final String COLUMN_URL = "Url";
    public static final String COLUMN_TOPIC_ID = "TopicId";
    public static final String COLUMN_POST_ID = "PostId";
    public static final String COLUMN_USER_ID = "UserId";
    public static final String COLUMN_USER = "User";
    public static final String COLUMN_TOPIC = "Topic";
    public static final String COLUMN_DATE = "Date";

    public static void insertRow(String title, String body, String url, CharSequence topicId, String topic,
                                 String postId, String userId, String user) {
        SQLiteDatabase db = null;

        try {
            NotesDbHelper dbHelper = new NotesDbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();
            // db.beginTransaction();

            ContentValues values = new ContentValues();

            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_BODY, body);
            values.put(COLUMN_URL, url);
            values.put(COLUMN_TOPIC_ID, topicId.toString());
            values.put(COLUMN_POST_ID, postId);
            values.put(COLUMN_USER_ID, userId);
            values.put(COLUMN_USER, user);
            values.put(COLUMN_TOPIC, topic);
            values.put(COLUMN_DATE, DbHelper.DateTimeFormat.format(new Date()));


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

    public static ArrayList<Note> getNotes(String topicId) throws ParseException, IOException {
        SQLiteDatabase db = null;

        try {
            NotesDbHelper dbHelper = new NotesDbHelper(App.getInstance());
            db = dbHelper.getReadableDatabase();
            return getNotes(db, topicId);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static ArrayList<Note> getNotes(SQLiteDatabase db, String topicId) throws ParseException {
        ArrayList<Note> notes = new ArrayList<>();

        Cursor c = null;
        try {


            String selection = null;
            String[] selectionArgs = null;

            if (!TextUtils.isEmpty(topicId)) {
                selection = COLUMN_TOPIC_ID + "=?";
                selectionArgs = new String[]{topicId};
            }
            c = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, COLUMN_DATE + " DESC");

            if (c.moveToFirst()) {
                int columnIdIndex = c.getColumnIndex(COLUMN_ID);
                int columnTitleIndex = c.getColumnIndex(COLUMN_TITLE);
                int columnBodyIndex = c.getColumnIndex(COLUMN_BODY);
                int columnUrlIndex = c.getColumnIndex(COLUMN_URL);
                int columnTopicIdIndex = c.getColumnIndex(COLUMN_TOPIC_ID);
                int columnPostIdIndex = c.getColumnIndex(COLUMN_POST_ID);
                int columnUserIdIndex = c.getColumnIndex(COLUMN_USER_ID);
                int columnUserIndex = c.getColumnIndex(COLUMN_USER);
                int columnTopicIndex = c.getColumnIndex(COLUMN_TOPIC);
                int columnDateIndex = c.getColumnIndex(COLUMN_DATE);
                do {
                    Note note = new Note();
                    note.Id = c.getString(columnIdIndex);
                    note.Title = c.getString(columnTitleIndex);
                    note.Body = c.getString(columnBodyIndex);
                    note.Url = c.getString(columnUrlIndex);
                    note.TopicId = c.getString(columnTopicIdIndex);
                    note.PostId = c.getString(columnPostIdIndex);
                    note.UserId = c.getString(columnUserIdIndex);
                    note.User = c.getString(columnUserIndex);
                    note.Topic = c.getString(columnTopicIndex);
                    note.Date = DbHelper.parseDate(c.getString(columnDateIndex));
                    notes.add(note);
                } while (c.moveToNext());
            }
        } finally {

            if (c != null)
                c.close();

        }

        return notes;
    }

    public static Note getNote(String id) throws IOException, ParseException {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            NotesDbHelper dbHelper = new NotesDbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();
            String selection = "_id=?";

            String[] selectionArgs = new String[]{id};

            c = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, COLUMN_DATE + " DESC");

            if (c.moveToFirst()) {

                int columnTitleIndex = c.getColumnIndex(COLUMN_TITLE);
                int columnBodyIndex = c.getColumnIndex(COLUMN_BODY);
                int columnUrlIndex = c.getColumnIndex(COLUMN_URL);
                int columnTopicIdIndex = c.getColumnIndex(COLUMN_TOPIC_ID);
                int columnPostIdIndex = c.getColumnIndex(COLUMN_POST_ID);
                int columnUserIdIndex = c.getColumnIndex(COLUMN_USER_ID);
                int columnUserIndex = c.getColumnIndex(COLUMN_USER);
                int columnTopicIndex = c.getColumnIndex(COLUMN_TOPIC);
                int columnDateIndex = c.getColumnIndex(COLUMN_DATE);


                Note note = new Note();
                note.Id = id;
                note.Title = c.getString(columnTitleIndex);
                note.Body = c.getString(columnBodyIndex);
                note.Url = c.getString(columnUrlIndex);
                note.TopicId = c.getString(columnTopicIdIndex);
                note.PostId = c.getString(columnPostIdIndex);
                note.UserId = c.getString(columnUserIdIndex);
                note.User = c.getString(columnUserIndex);
                note.Topic = c.getString(columnTopicIndex);
                note.Date = DbHelper.parseDate(c.getString(columnDateIndex));

                return note;


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

    public static void delete(String id) throws IOException {

        SQLiteDatabase db = null;
        try {

            NotesDbHelper dbHelper = new NotesDbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();

            db.execSQL("delete from " + TABLE_NAME + " where " + COLUMN_ID + "=" + id);


        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
