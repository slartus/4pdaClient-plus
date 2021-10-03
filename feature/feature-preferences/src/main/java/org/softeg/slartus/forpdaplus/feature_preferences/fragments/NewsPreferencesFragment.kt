package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import org.softeg.slartus.forpdaplus.feature_preferences.R

@Suppress("unused")
class NewsPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.news_preferences, rootKey)
    }
}