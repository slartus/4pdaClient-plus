package org.softeg.slartus.forpdaplus.core_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.softeg.slartus.forpdaplus.core_db.note.Note
import org.softeg.slartus.forpdaplus.core_db.note.NoteDao
import org.softeg.slartus.forpdaplus.core_db.settings.Setting

@Database(entities = [Setting::class, Note::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        const val NAME = "forpda.db"
    }
}