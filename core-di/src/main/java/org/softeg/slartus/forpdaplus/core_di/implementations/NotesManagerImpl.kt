package org.softeg.slartus.forpdaplus.core_di.implementations

import javax.inject.Inject

class NotesManagerImpl @Inject constructor(private val notesManager: org.softeg.slartus.forpdaplus.feature_notes.NotesManager) :
    org.softeg.slartus.forpdaplus.feature_preferences.di.NotesManager {
    override fun backupNotes() {
        notesManager.backupNotes()
    }

    override fun restoreNotes() {
        notesManager.restoreNotes()
    }
}