package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppNavigator
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppScreen
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppService
import org.softeg.slartus.forpdaplus.feature_preferences.Dialogs
import org.softeg.slartus.forpdaplus.feature_preferences.R
import javax.inject.Inject

@Suppress("unused")
@AndroidEntryPoint
class AboutPreferencesFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var appNavigator: AppNavigator

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "About.AppVersion" -> {
                Dialogs.showAbout(requireContext())
                return true
            }
            "About.History" -> {
                Dialogs.showAboutHistory(requireContext())
                return true
            }
            "About.ShareIt" -> {
                Dialogs.showShareIt(requireContext())
                return true
            }
            "About.ShowTheme" -> {
                showTheme("271502")
                return true
            }
            "About.CheckNewVersion" -> {
                checkUpdates()
                return true
            }
            "About.OpenThemeForPda" -> {
                showTheme("820313")
                return true
            }
        }
        return false
    }

    private fun showTheme(themeId: String) {
        appNavigator.navigateTo(AppScreen.Topic(themeId))
        activity?.finish()
    }

    private fun checkUpdates() {
        appNavigator.startService(AppService.VersionChecker)
    }
}