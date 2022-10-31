package org.softeg.slartus.forpdaplus.forum.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ForumEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ForumDatabase : RoomDatabase() {
    abstract fun forumDao(): ForumDao

    companion object {
        const val NAME = "forum.db"
    }
}