package org.softeg.slartus.forpdaplus.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils

import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.listfragments.next.ForumFragment

import java.io.IOException
import java.util.ArrayList

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 20.11.12
 * Time: 9:25
 * To change this template use File | Settings | File Templates.
 */
object ForumsTable {
    private const val TABLE_NAME = "Forums"
    private const val COLUMN_ID = "_id"
    private const val COLUMN_PARENT_ID = "ParentId"
    private const val COLUMN_TITLE = "Title"
    private const val COLUMN_DESCRIPTION = "Description"
    private const val COLUMN_GLOBALSORTORDER = "GlobalSortOrder"
    private const val COLUMN_HAS_TOPICS = "HasTopics"
    private const val COLUMN_HAS_FORUMS = "HasForums"
    private const val COLUMN_ICON_URL = "IconUrl"


    /**
     * для указанного форума восстанавливает путь до корня
     */
    @Throws(IOException::class)
    fun loadForumTitlesList(forumIds: Collection<String>): List<String> {

        var c: Cursor? = null
        var db: SQLiteDatabase? = null
        val res = ArrayList<String>()
        try {
            val dbHelper = ForumStructDbHelper(App.getInstance())
            db = dbHelper.writableDatabase

            assert(db != null)


            var forumIdsString = TextUtils.join("','", forumIds).replace("all", "-1")
            if (!TextUtils.isEmpty(forumIdsString))
                forumIdsString = "'$forumIdsString'"

            val selection = "$COLUMN_ID in ($forumIdsString)"

            c = db!!.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_TITLE),
                    selection, null, null, null, COLUMN_GLOBALSORTORDER)

            while (c!!.moveToNext()) {
                val columnTitleIndex = c.getColumnIndex(COLUMN_TITLE)
                val title = c.getString(columnTitleIndex)
                res.add(title)
            }
        } finally {
            c?.close()
            db?.close()
        }
        return res
    }


    @Throws(IOException::class)
    fun updateForums(forumItems: ArrayList<Forum>) {
        var db: SQLiteDatabase? = null
        try {
            val dbHelper = ForumStructDbHelper(App.getInstance())
            db = dbHelper.writableDatabase

            db!!.beginTransaction()
            db.execSQL("delete from $TABLE_NAME")

            for (i in forumItems.indices) {
                val item = forumItems[i]
                val values = ContentValues()
                values.put(COLUMN_ID, item.id)

                values.put(COLUMN_PARENT_ID, item.parentId)
                values.put(COLUMN_TITLE, item.title)
                values.put(COLUMN_DESCRIPTION, if (item.description != null) item.description else null)
                values.put(COLUMN_GLOBALSORTORDER, Integer.toString(i))
                values.put(COLUMN_HAS_TOPICS, if (item.isHasTopics) 1 else 0)
                values.put(COLUMN_HAS_FORUMS, if (item.isHasForums) 1 else 0)
                values.put(COLUMN_ICON_URL, item.iconUrl)
                db.insertOrThrow(TABLE_NAME, null, values)
            }

            db.setTransactionSuccessful()
        } finally {
            if (db != null) {
                db.endTransaction()
                db.close()
            }
        }

    }

    @Throws(IOException::class)
    fun getForums(forumId: String?): ForumFragment.ForumBranch {
        val res = ForumFragment.ForumBranch()

        var db: SQLiteDatabase? = null
        var c: Cursor? = null
        try {
            val dbHelper = ForumStructDbHelper(App.getInstance())
            db = dbHelper.readableDatabase
            assert(db != null)

            res.crumbs.add(Forum(null, "4PDA"))
            if (forumId != null)
                loadForumsUp(db!!, forumId, res.crumbs)


            // получаем чайлдов
            var selection = "$COLUMN_PARENT_ID=?"
            var selectionArgs: Array<String?>? = arrayOf(forumId)
            if (forumId == null) {
                selection = "$COLUMN_PARENT_ID ISNULL"
                selectionArgs = null
            }
            c = db!!.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_HAS_TOPICS, COLUMN_HAS_FORUMS, COLUMN_ICON_URL),
                    selection, selectionArgs, null, null, COLUMN_GLOBALSORTORDER)
            if (c!!.moveToFirst()) {

                val columnIdIndex = c.getColumnIndex(COLUMN_ID)
                val columnTitleIndex = c.getColumnIndex(COLUMN_TITLE)
                val columnDescriptionIndex = c.getColumnIndex(COLUMN_DESCRIPTION)
                val columnHasTopicsIndex = c.getColumnIndex(COLUMN_HAS_TOPICS)
                val columnHasForumsIndex = c.getColumnIndex(COLUMN_HAS_FORUMS)
                val columnIconUrlIndex = c.getColumnIndex(COLUMN_ICON_URL)

                do {
                    val id = c.getString(columnIdIndex)
                    val title = c.getString(columnTitleIndex)
                    val description = c.getString(columnDescriptionIndex)
                    val hasTopics = c.getShort(columnHasTopicsIndex).toInt() == 1
                    val hasForums = c.getShort(columnHasForumsIndex).toInt() == 1
                    val iconUrl = c.getString(columnIconUrlIndex)

                    val forum = Forum(id, title)
                    forum.isHasTopics = hasTopics
                    forum.description = description
                    forum.isHasForums = hasForums
                    forum.iconUrl = iconUrl
                    res.items.add(forum)
                } while (c.moveToNext())
            }
        } finally {
            if (db != null) {
                c?.close()
                db.close()
            }
        }

        return res
    }

    private fun getForum(id: String, c: Cursor): Forum {
        val columnParentIdIndex = c.getColumnIndex(COLUMN_PARENT_ID)
        val columnTitleIndex = c.getColumnIndex(COLUMN_TITLE)
        val columnDescriptionIndex = c.getColumnIndex(COLUMN_DESCRIPTION)
        val columnHasTopicsIndex = c.getColumnIndex(COLUMN_HAS_TOPICS)
        val columnHasForumsIndex = c.getColumnIndex(COLUMN_HAS_FORUMS)
        val columnIconUrlIndex = c.getColumnIndex(COLUMN_ICON_URL)

        val parentId = c.getString(columnParentIdIndex)
        val title = c.getString(columnTitleIndex)
        val description = c.getString(columnDescriptionIndex)
        val hasTopics = c.getShort(columnHasTopicsIndex).toInt() == 1
        val hasForums = c.getShort(columnHasForumsIndex).toInt() == 1
        val iconUrl = c.getString(columnIconUrlIndex)

        val forum = Forum(id, title)
        forum.isHasTopics = hasTopics
        forum.iconUrl = iconUrl
        forum.description = description
        forum.parentId = parentId
        forum.isHasForums = hasForums
        return forum
    }

    private fun loadForumsUp(db: SQLiteDatabase, id: String, forums: MutableList<Forum>) {
        var c: Cursor? = null
        try {
            val selection = "$COLUMN_ID=?"
            val selectionArgs = arrayOf(id)
            c = db.query(TABLE_NAME, arrayOf(COLUMN_PARENT_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_HAS_TOPICS, COLUMN_HAS_FORUMS, COLUMN_ICON_URL),
                    selection, selectionArgs, null, null, COLUMN_GLOBALSORTORDER)
            if (c!!.moveToFirst()) {
                val forum = getForum(id, c)
                forums.add(1, forum)

                if (forum.parentId != null)
                    loadForumsUp(db, forum.parentId, forums)
            }
        } finally {
            c?.close()
        }
    }


}
