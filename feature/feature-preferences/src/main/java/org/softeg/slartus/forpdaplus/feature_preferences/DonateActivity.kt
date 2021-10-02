package org.softeg.slartus.forpdaplus.feature_preferences

import org.softeg.slartus.forpdaplus.core_ui.AppTheme.prefsThemeStyleResID
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.softeg.slartus.forpdaplus.feature_preferences.fragments.DonatePreferencesFragment

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 18.10.12
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
class DonateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(prefsThemeStyleResID)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, DonatePreferencesFragment())
                .commitAllowingStateLoss()
        }
    }
}