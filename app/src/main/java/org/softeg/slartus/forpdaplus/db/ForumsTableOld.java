package org.softeg.slartus.forpdaplus.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.Forum;


import java.io.IOException;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 20.11.12
 * Time: 9:25
 * To change this template use File | Settings | File Templates.
 */
public class ForumsTableOld {
    public static final String TABLE_NAME = "Forums";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PARENT_ID = "ParentId";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_GLOBALSORTORDER = "GlobalSortOrder";
    public static final String COLUMN_HAS_TOPICS = "HasTopics";

    public static Forum loadForumsTree() throws IOException {
        return loadForumsTree(false);
    }

    public static Forum loadForumsTree(Boolean addTopicsItem) throws IOException {
        Forum mainForum = new Forum("-1", "4PDA");


        SQLiteDatabase db = null;
        Cursor c = null;
        HashMap<String, Forum> hashMap = new HashMap<>();
        try {
            ForumStructDbHelper dbHelper = new ForumStructDbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();

            assert db != null;
            c = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_PARENT_ID, COLUMN_TITLE, COLUMN_HAS_TOPICS},
                    null, null, null, null, COLUMN_GLOBALSORTORDER);

            if (c.moveToFirst()) {
                int columnIdIndex = c.getColumnIndex(COLUMN_ID);
                int columnParentIdIndex = c.getColumnIndex(COLUMN_PARENT_ID);
                int columnTitleIndex = c.getColumnIndex(COLUMN_TITLE);
                int columnHasTopicsIndex = c.getColumnIndex(COLUMN_HAS_TOPICS);
                

                do {
                    String id = c.getString(columnIdIndex);
                    String parentId = c.isNull(columnParentIdIndex) ? null : c.getString(columnParentIdIndex);
                    String title = c.getString(columnTitleIndex);
                    Boolean hasTopics = addTopicsItem && c.getShort(columnHasTopicsIndex) == 1;

                    Forum forum = new Forum(id, title);

                    if (hasTopics) {
                        Forum topicsItem = new Forum(id, title + " @ темы");
                        forum.addForum(topicsItem);
                    }
                    Forum parentForum = parentId == null ? mainForum : hashMap.get(parentId);
                    if (parentForum != null)
                        parentForum.addForum(forum);

                    hashMap.put(id, forum);


                } while (c.moveToNext());
            }
            if (addTopicsItem)
                clearForums(mainForum);
        } finally {
            if (db != null) {
                if (c != null)
                    c.close();
                db.close();
            }
        }

        return mainForum;
    }

    private static void clearForums(Forum mainForum) {
        if (mainForum.getForums().size() == 1 && mainForum.getForums().get(0).getTitle().endsWith(" @ темы"))
            mainForum.getForums().clear();
        else {
            for (Forum child : mainForum.getForums()) {
                clearForums(child);
            }
        }
    }

}
