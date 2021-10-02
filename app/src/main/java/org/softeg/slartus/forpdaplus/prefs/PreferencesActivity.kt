package org.softeg.slartus.forpdaplus.prefs

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.AppTheme.getThemeCssFileName
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.feature_preferences.Preferences
import org.softeg.slartus.forpdaplus.styles.CssStyle
import ru.slartus.http.PersistentCookieStore.Companion.getInstance
import java.io.File
import java.util.*

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
        val ft = supportFragmentManager.beginTransaction()
        val fragment = PrefsFragment()
        val args = Bundle()
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.key)
        fragment.arguments = args
        ft.replace(android.R.id.content, fragment, preferenceScreen.key)
        ft.addToBackStack(preferenceScreen.key)
        ft.commit()
        return true
    }

    public override fun onStop() {
        super.onStop()

        App.resStartNotifierServices()
        getInstance(App.getInstance()).reload()
    }

    companion object {
        private val appCookiesPath: String
            get() = Preferences.System.systemDir + "4pda_cookies"

        @JvmStatic
        val cookieFilePath: String
            get() {
                var res = App.getInstance().preferences.getString("cookies.path", "") ?: ""
                if (TextUtils.isEmpty(res)) res = appCookiesPath
                return res.replace("/", File.separator)
            }

        @JvmStatic
        fun getStylesList(
            context: Context,
            newStyleNames: ArrayList<CharSequence>,
            newstyleValues: ArrayList<CharSequence>
        ) {
            var xmlPath: String
            var cssStyle: CssStyle
            val styleNames = context.resources.getStringArray(R.array.appthemesArray)
            val styleValues = context.resources.getStringArray(R.array.appthemesValues)
            for (i in styleNames.indices) {
                var styleName: CharSequence = styleNames[i]
                val styleValue: CharSequence = styleValues[i]
                xmlPath = getThemeCssFileName(styleValue.toString()).replace(".css", ".xml")
                    .replace("/android_asset/", "")
                cssStyle = CssStyle.parseStyleFromAssets(context, xmlPath)
                if (cssStyle.ExistsInfo) styleName = cssStyle.Title
                newStyleNames.add(styleName)
                newstyleValues.add(styleValue)
            }
            val file = File(Preferences.System.systemDir + "styles/")
            getStylesList(newStyleNames, newstyleValues, file)
        }

        private fun getStylesList(
            newStyleNames: ArrayList<CharSequence>,
            newstyleValues: ArrayList<CharSequence>, file: File
        ) {
            var cssPath: String
            var xmlPath: String
            var cssStyle: CssStyle
            if (file.exists()) {
                val cssFiles = file.listFiles() ?: return
                for (cssFile in cssFiles) {
                    if (cssFile.isDirectory) {
                        getStylesList(newStyleNames, newstyleValues, cssFile)
                        continue
                    }
                    cssPath = cssFile.path
                    if (!cssPath.lowercase(Locale.getDefault()).endsWith(".css")) continue
                    xmlPath = cssPath.replace(".css", ".xml")
                    cssStyle = CssStyle.parseStyleFromFile(xmlPath)
                    val title = cssStyle.Title
                    newStyleNames.add(title)
                    newstyleValues.add(cssPath)
                }
            }
        }

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