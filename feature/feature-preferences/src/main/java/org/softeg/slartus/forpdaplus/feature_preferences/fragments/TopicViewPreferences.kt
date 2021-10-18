package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.os.Bundle
import androidx.preference.Preference
import org.softeg.slartus.forpdaplus.core_ui.ui.fragments.BasePreferenceFragment
import org.softeg.slartus.forpdaplus.feature_preferences.R

class TopicViewPreferences : BasePreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.topic_view_preferences, rootKey)
    }
    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {

        }
        return super.onPreferenceTreeClick(preference)
    }
}