package org.softeg.slartus.forpdaplus.core_di.implementations

import org.softeg.slartus.forpdaplus.feature_notes.di.NotesPreferences
import org.softeg.slartus.forpdaplus.feature_preferences.Preferences
import javax.inject.Inject

class NotesPreferencesImpl @Inject constructor() : NotesPreferences {
    override fun setPlacement(placement: String) {
        Preferences.Notes.setPlacement(placement)
    }

    override val isLocal
        get() = Preferences.Notes.isLocal

    override var remoteUrl: String?
        get() = Preferences.Notes.remoteUrl
        set(value) {
            Preferences.Notes.remoteUrl = value ?: ""
        }
}