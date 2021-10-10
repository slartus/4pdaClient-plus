package org.softeg.slartus.forpdaplus.core_di.implementations

import android.content.Context
import org.softeg.slartus.forpdaplus.feature_notes.AppDatabase
import java.io.File
import javax.inject.Inject

class AppDatabaseImpl @Inject constructor(
    private val appDatabase: org.softeg.slartus.forpdaplus.core_db.AppDatabase
) : AppDatabase {
    override fun close() {
        appDatabase.close()
    }

    override fun getDatabasePath(context: Context): File =
        context.getDatabasePath(org.softeg.slartus.forpdaplus.core_db.AppDatabase.NAME)
}