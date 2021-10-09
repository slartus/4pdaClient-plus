package org.softeg.slartus.forpdaplus.feature_notes

import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

class NotesManager @Inject constructor(private val notesDao: NotesDao) {
    fun restoreNotes() {
        // !TODO: сделать нормальный вызов корутин
        runBlocking {
            notesDao.getAll()
        }
        Timber.d("restoreNotes")
    }

    fun backupNotes() {
        runBlocking {
            notesDao.getAll()
        }
        Timber.d("backupNotes")
    }
}