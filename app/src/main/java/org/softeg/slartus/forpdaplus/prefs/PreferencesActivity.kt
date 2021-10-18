package org.softeg.slartus.forpdaplus.prefs

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import dagger.hilt.android.AndroidEntryPoint
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.feature_notes.ui.NotesPreferencesFragment
import org.softeg.slartus.forpdaplus.feature_preferences.fragments.TopicViewPreferences
import org.softeg.slartus.forpdaplus.log.ActivityTimberTree
import ru.slartus.http.PersistentCookieStore.Companion.getInstance
import timber.log.Timber

/**
 * User: slinkin
 * Date: 03.10.11
 * Time: 10:47
 */
@AndroidEntryPoint
class PreferencesActivity : BasePreferencesActivity(),
    PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    private val timberTree = ActivityTimberTree(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, PrefsFragment())
                .commitAllowingStateLoss()
        }
    }

    override fun onPreferenceStartScreen(
        preferenceFragmentCompat: PreferenceFragmentCompat?,
        preferenceScreen: PreferenceScreen
    ): Boolean {
        val fragment =
            when (preferenceScreen.key) {
                "download_files_screen" -> {
                    TopicViewPreferences()
                }
                "notes" -> {
                    NotesPreferencesFragment()
                }
                else -> {
                    PrefsFragment()
                }
            }

        fragment.arguments = Bundle().apply {
            putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.key)
        }
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment, preferenceScreen.key)
            .addToBackStack(preferenceScreen.key)
            .commit()
        return true
    }

    override fun onStart() {
        super.onStart()

        Timber.plant(timberTree)
    }

    public override fun onStop() {
        super.onStop()
        Timber.uproot(timberTree)
        App.resStartNotifierServices()
        getInstance(App.getInstance()).reload()
    }

}
