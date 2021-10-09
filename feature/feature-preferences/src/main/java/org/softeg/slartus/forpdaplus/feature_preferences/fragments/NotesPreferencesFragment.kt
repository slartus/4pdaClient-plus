package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import org.softeg.slartus.forpdaplus.feature_preferences.Dialogs
import org.softeg.slartus.forpdaplus.feature_preferences.R
import org.softeg.slartus.forpdaplus.feature_preferences.di.NotesManager
import javax.inject.Inject

@Suppress("unused")
@AndroidEntryPoint
class NotesPreferencesFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var notesManager: NotesManager
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notes_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "notes.backup" -> {
                notesManager.backupNotes()
            }
            "notes.restore" -> {
                notesManager.restoreNotes()
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}