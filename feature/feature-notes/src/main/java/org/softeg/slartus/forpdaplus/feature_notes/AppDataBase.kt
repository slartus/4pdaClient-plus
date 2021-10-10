package org.softeg.slartus.forpdaplus.feature_notes

import android.content.Context
import java.io.File

interface AppDatabase {
    fun close()
    fun getDatabasePath(context: Context): File
}