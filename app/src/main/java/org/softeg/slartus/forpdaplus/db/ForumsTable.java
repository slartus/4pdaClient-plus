package org.softeg.slartus.forpdaplus.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.softeg.slartus.forpdaapi.Forum;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.listfragments.next.ForumFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 20.11.12
 * Time: 9:25
 * To change this template use File | Settings | File Templates.
 */
public class ForumsTable {
    public static final String TABLE_NAME = "Forums";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PARENT_ID = "ParentId";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String COLUMN_GLOBALSORTORDER = "GlobalSortOrder";
    public static final String COLUMN_HAS_TOPICS = "HasTopics";
    public static final String COLUMN_HAS_FORUMS = "HasForums";

    /**
     * для указанного форума восстанавливает путь до корня
     */
    public static Forum loadCrumbs(String forumId, Boolean addTopicsItem) throws IOException {

        SQLiteDatabase db = null;

        try {
            ForumStructDbHelper dbHelper = new ForumStructDbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();

            assert db != null;

            return loadCrumbs(db, forumId, addTopicsItem);
        } finally {
            if (db != null) {

                db.close();
            }
        }
    }


    /**
     * для указанного форума восстанавливает путь до корня
     */
    public static List<String> loadForumTitlesList(Collection<String> forumIds) throws IOException {

        Cursor c = null;
        SQLiteDatabase db = null;
        ArrayList<String> res = new ArrayList<String>();
        try {
            ForumStructDbHelper dbHelper = new ForumStructDbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();

            assert db != null;


            String forumIdsString = TextUtils.join("','", forumIds).replace("all", "-1");
            if (!TextUtils.isEmpty(forumIdsString))
                forumIdsString = "'" + forumIdsString + "'";

            String selection = COLUMN_ID + " in (" + forumIdsString + ")";

            c = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TITLE},
                    selection, null, null, null, COLUMN_GLOBALSORTORDER);

            while (c.moveToNext()) {
                int columnTitleIndex = c.getColumnIndex(COLUMN_TITLE);
                String title = c.getString(columnTitleIndex);
                res.add(title);
            }
        } finally {
            if (c != null)
                c.close();
            if (db != null) {

                db.close();
            }
        }
        return res;
    }

    /**
     * для указанного форума восстанавливает путь до корня
     */
    public static Forum loadCrumbs(SQLiteDatabase db, String forumId, Boolean addTopicsItem) throws IOException {
        Cursor c = null;
        try {
            String selection = COLUMN_ID + "=?";
            String[] selectionArgs = new String[]{forumId};
            c = db.query(TABLE_NAME, new String[]{COLUMN_PARENT_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_HAS_TOPICS},
                    selection, selectionArgs, null, null, COLUMN_GLOBALSORTORDER);

            if (c.moveToFirst()) {
                int columnParentIdIndex = c.getColumnIndex(COLUMN_PARENT_ID);
                int columnTitleIndex = c.getColumnIndex(COLUMN_TITLE);
                int columnDescriptionIndex = c.getColumnIndex(COLUMN_DESCRIPTION);
                int columnHasTopicsIndex = c.getColumnIndex(COLUMN_HAS_TOPICS);
                String parentId = c.getString(columnParentIdIndex);

                String title = c.getString(columnTitleIndex);
                String description = c.getString(columnDescriptionIndex);
                Boolean hasTopics = addTopicsItem && c.getShort(columnHasTopicsIndex) == 1;

                Forum f = new Forum(forumId, title);
                f.setHasTopics(hasTopics);
                f.setDescription(description);

                Forum parentForum;
                if (TextUtils.isEmpty(parentId)) {
                    parentForum = new Forum("-1", "4pda");
                } else {
                    parentForum = loadCrumbs(db, parentId, addTopicsItem);
                }
                f.setParent(parentForum);
                return f;
            }
        } finally {
            if (c != null)
                c.close();
        }
        return null;
    }

    public static void updateForums(ArrayList<Forum> forumItems) throws IOException {
        SQLiteDatabase db = null;
        try {
            ForumStructDbHelper dbHelper = new ForumStructDbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();

            db.beginTransaction();
            db.execSQL("delete from " + TABLE_NAME);

            for (int i = 0; i < forumItems.size(); i++) {
                Forum item = forumItems.get(i);
                ContentValues values = new ContentValues();
                values.put(COLUMN_ID, item.getId());

                values.put(COLUMN_PARENT_ID, item.getParent() != null ? item.getParent().getId().toString() : null);
                values.put(COLUMN_TITLE, item.getTitle());
                values.put(COLUMN_DESCRIPTION, item.getDescription() != null ? item.getDescription().toString() : null);
                values.put(COLUMN_GLOBALSORTORDER, Integer.toString(i));
                values.put(COLUMN_HAS_TOPICS, item.isHasTopics() ? 1 : 0);
                values.put(COLUMN_HAS_FORUMS, item.isHasForums() ? 1 : 0);
                db.insertOrThrow(TABLE_NAME, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

    }

    public static ForumFragment.ForumBranch getForums(String forumId) throws IOException {
        ForumFragment.ForumBranch res = new ForumFragment.ForumBranch();

        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            ForumStructDbHelper dbHelper = new ForumStructDbHelper(App.getInstance());
            db = dbHelper.getReadableDatabase();
            assert db != null;

            res.getCrumbs().add(new Forum(null, "4PDA"));
            if (forumId != null)
                loadForumsUp(db, forumId, res.getCrumbs());


            // получаем чайлдов
            String selection = COLUMN_PARENT_ID + "=?";
            String[] selectionArgs = new String[]{forumId};
            if (forumId == null) {
                selection = COLUMN_PARENT_ID + " ISNULL";
                selectionArgs = null;
            }
            c = db.query(TABLE_NAME, new String[]{COLUMN_ID,  COLUMN_TITLE,
                            COLUMN_DESCRIPTION, COLUMN_HAS_TOPICS},
                    selection, selectionArgs, null, null, COLUMN_GLOBALSORTORDER);
            if (c.moveToFirst()) {

                int columnIdIndex = c.getColumnIndex(COLUMN_ID);

                int columnTitleIndex = c.getColumnIndex(COLUMN_TITLE);
                int columnDescriptionIndex = c.getColumnIndex(COLUMN_DESCRIPTION);
                int columnHasTopicsIndex = c.getColumnIndex(COLUMN_HAS_TOPICS);

                do {
                    String id = c.getString(columnIdIndex);
                    String title = c.getString(columnTitleIndex);
                    String description = c.getString(columnDescriptionIndex);
                    Boolean hasTopics = c.getShort(columnHasTopicsIndex) == 1;

                    Forum forum = new Forum(id, title);
                    forum.setHasTopics(hasTopics);
                    forum.setDescription(description);

                    res.getItems().add(forum);
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

    private static void loadForumsUp(SQLiteDatabase db, String id, List<Forum> forums) {
        Cursor c = null;
        try {
            String selection = COLUMN_ID + "=?";
            String[] selectionArgs = new String[]{id};
            c = db.query(TABLE_NAME, new String[]{COLUMN_PARENT_ID, COLUMN_TITLE,
                            COLUMN_DESCRIPTION, COLUMN_HAS_TOPICS},
                    selection, selectionArgs, null, null, COLUMN_GLOBALSORTORDER);
            if (c.moveToFirst()) {
                int columnParentIdIndex = c.getColumnIndex(COLUMN_PARENT_ID);
                int columnTitleIndex = c.getColumnIndex(COLUMN_TITLE);
                int columnDescriptionIndex = c.getColumnIndex(COLUMN_DESCRIPTION);
                int columnHasTopicsIndex = c.getColumnIndex(COLUMN_HAS_TOPICS);

                String parentId = c.getString(columnParentIdIndex);
                String title = c.getString(columnTitleIndex);
                String description = c.getString(columnDescriptionIndex);
                Boolean hasTopics = c.getShort(columnHasTopicsIndex) == 1;

                Forum forum = new Forum(id, title);
                forum.setHasTopics(hasTopics);
                forum.setDescription(description);

                    forums.add(1, forum);

                if (parentId != null)
                    loadForumsUp(db, parentId, forums);
            }
        } finally {
            if (db != null) {
                if (c != null)
                    c.close();
            }
        }
    }

    public static ArrayList<Forum> loadForums(Forum parentForum, Boolean addTopicsItem) throws IOException {
        ArrayList<Forum> res = new ArrayList<Forum>();

        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            ForumStructDbHelper dbHelper = new ForumStructDbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();

            assert db != null;
            Boolean isRoot = parentForum == null || "-1".equals(parentForum.getId());
            String selection = COLUMN_PARENT_ID + (isRoot ? " is null" : "=?");
            String[] selectionArgs = isRoot ? null : new String[]{parentForum.getId()};
            c = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_HAS_TOPICS},
                    selection, selectionArgs, null, null, COLUMN_GLOBALSORTORDER);

            if (parentForum != null && parentForum.isHasTopics()) {
                Forum topicsItem = new Forum(parentForum.getId(), parentForum.getTitle() + "@темы");
                topicsItem.setParent(parentForum);
                topicsItem.setDescription("Темы, нераспределённые по разделам");
                res.add(topicsItem);
            }

            if (c.moveToFirst()) {

                int columnIdIndex = c.getColumnIndex(COLUMN_ID);
                int columnTitleIndex = c.getColumnIndex(COLUMN_TITLE);
                int columnDescriptionIndex = c.getColumnIndex(COLUMN_DESCRIPTION);
                int columnHasTopicsIndex = c.getColumnIndex(COLUMN_HAS_TOPICS);

                do {
                    String id = c.getString(columnIdIndex);
                    String title = c.getString(columnTitleIndex);
                    String description = c.getString(columnDescriptionIndex);
                    Boolean hasTopics = addTopicsItem && c.getShort(columnHasTopicsIndex) == 1;

                    Forum forum = new Forum(id, title);
                    forum.setHasTopics(hasTopics);
                    forum.setDescription(description);
                    forum.setParent(parentForum);
                    res.add(forum);
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
}
