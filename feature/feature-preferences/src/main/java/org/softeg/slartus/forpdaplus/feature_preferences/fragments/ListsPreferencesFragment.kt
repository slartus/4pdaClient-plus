package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.os.Bundle
import org.softeg.slartus.forpdaplus.core_ui.ui.fragments.BasePreferenceFragment
import org.softeg.slartus.forpdaplus.feature_preferences.R

@Suppress("unused")
class ListsPreferencesFragment : BasePreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.lists_preferences, rootKey)
    }
}