package org.softeg.slartus.forpdaplus.prefs

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.feature_preferences.fragments.TopicViewPreferences
import ru.slartus.http.PersistentCookieStore.Companion.getInstance

/**
 * User: slinkin
 * Date: 03.10.11
 * Time: 10:47
 */
class PreferencesActivity : BasePreferencesActivity(),
    PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

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

    public override fun onStop() {
        super.onStop()

        App.resStartNotifierServices()
        getInstance(App.getInstance()).reload()
    }

    companion object {



        @JvmStatic
        val packageInfo: PackageInfo
            get() {
                val packageName = App.getInstance().packageName
                try {
                    return App.getInstance().packageManager.getPackageInfo(
                        packageName, PackageManager.GET_META_DATA
                    )
                } catch (e1: PackageManager.NameNotFoundException) {
                    AppLog.e(App.getInstance(), e1)
                }
                val packageInfo = PackageInfo()
                packageInfo.packageName = packageName
                packageInfo.versionName = "unknown"
                packageInfo.versionCode = 1
                return packageInfo
            }
        val programFullName: String
            get() {
                var programName = App.getInstance().getString(R.string.app_name)
                val pInfo = packageInfo
                programName += " v" + pInfo.versionName + " c" + pInfo.versionCode
                return programName
            }
    }
}
