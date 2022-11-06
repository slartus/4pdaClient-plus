package org.softeg.slartus.forpdaplus.qms.data.screens.contacts.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [QmsContactEntity::class],
    version = 1
)

abstract class QmsDatabase : RoomDatabase() {
    abstract fun qmsContactsDao(): QmsContactsDao

    companion object {
        const val NAME = "qms.db"
    }
}