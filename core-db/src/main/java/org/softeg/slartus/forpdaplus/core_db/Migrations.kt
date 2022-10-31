package org.softeg.slartus.forpdaplus.core_db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE `qms_contacts`")
        database.execSQL("CREATE TABLE `qms_contacts` (id INTEGER PRIMARY KEY NOT NULL, nick TEXT, avatarUrl TEXT, messagesCount INTEGER, sort INTEGER)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE `qms_contacts`")
    }
}