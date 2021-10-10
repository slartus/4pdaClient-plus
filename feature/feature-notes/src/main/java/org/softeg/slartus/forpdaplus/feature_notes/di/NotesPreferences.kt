package org.softeg.slartus.forpdaplus.feature_notes.di

interface NotesPreferences {
    fun setPlacement(placement: String)

    var remoteUrl: CharSequence?
}