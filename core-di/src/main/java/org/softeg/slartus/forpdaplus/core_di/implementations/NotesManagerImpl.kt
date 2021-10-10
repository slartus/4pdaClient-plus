package org.softeg.slartus.forpdaplus.core_di.implementations

import android.content.Context
import javax.inject.Inject

class NotesManagerImpl @Inject constructor(
    private val notesManager: org.softeg.slartus.forpdaplus.feature_notes.NotesManager
) :
    org.softeg.slartus.forpdaplus.feature_preferences.di.NotesManager {
    override fun backupNotes(context: Context) {
        notesManager.backupNotes(context)
    }

    override fun restoreNotes(context: Context) {
        notesManager.restoreNotes(context)
    }
}