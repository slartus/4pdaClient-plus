package org.softeg.slartus.forpdaplus.core_db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `Setting` (`id` INTEGER, " +
                "PRIMARY KEY(`id`))")
    }
}