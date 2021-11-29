package org.softeg.slartus.forpdaplus.core_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.softeg.slartus.forpdaplus.core_db.forum.Forum
import org.softeg.slartus.forpdaplus.core_db.forum.ForumDao

@Database(entities = [Forum::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun forumDao(): ForumDao

    companion object {
        const val NAME = "forpda.db"
    }
}