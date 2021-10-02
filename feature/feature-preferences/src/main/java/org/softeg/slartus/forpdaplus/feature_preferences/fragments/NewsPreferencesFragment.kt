package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.softeg.slartus.forpdacommon.StringUtils.copyToClipboard
import org.softeg.slartus.forpdacommon.openUrl
import org.softeg.slartus.forpdaplus.feature_preferences.R

class NewsPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.news_preferences, rootKey)
    }
    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {

        }
        return super.onPreferenceTreeClick(preference)
    }
}