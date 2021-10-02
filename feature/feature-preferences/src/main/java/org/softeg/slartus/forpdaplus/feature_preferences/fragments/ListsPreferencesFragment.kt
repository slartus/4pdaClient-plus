package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.softeg.slartus.forpdaplus.feature_preferences.R

class ListsPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.lists_preferences, rootKey)
    }
}