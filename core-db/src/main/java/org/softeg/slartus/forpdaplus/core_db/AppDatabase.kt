package org.softeg.slartus.forpdaplus.core_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.softeg.slartus.forpdaplus.core_db.forum.Forum
import org.softeg.slartus.forpdaplus.core_db.forum.ForumDao
import org.softeg.slartus.forpdaplus.core_db.qms_contacts.QmsContact
import org.softeg.slartus.forpdaplus.core_db.qms_contacts.QmsContactsDao

@Database(
    entities = [Forum::class, QmsContact::class], version = 5
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun forumDao(): ForumDao
    abstract fun qmsContactsDao(): QmsContactsDao

    companion object {
        const val NAME = "forpda.db"
    }
}