package org.softeg.slartus.forpdaplus.preferences

import android.content.Context
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
class NewsPreferencesTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<PreferencesActivity> =
        ActivityScenarioRule(PreferencesActivity::class.java)

    private val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun test() {
        appearanceClick(R.string.news)

        checkBoxPreferenceTest(
            context,
            R.string.send_post_confirm_dialog_ask,
            "news.ConfirmSend",
            true
        )
        listPreferenceTest(
            context,
            R.string.prefs_load_images,
            "news.LoadsImages",
            R.array.ConnectTypeArray,
            R.array.ConnectTypeValues,
            "1"
        )
        checkBoxPreferenceTest(
            context,
            R.string.prefs_use_volumes_scroll,
            "news.UseVolumesScroll",
            false
        )
        checkBoxPreferenceTest(
            context,
            R.string.prefs_keep_screen_on,
            "news.KeepScreenOn",
            false
        )
        checkBoxPreferenceTest(
            context,
            R.string.full_screen,
            "news.FullScreen",
            false
        )
        checkBoxPreferenceTest(
            context,
            R.string.dont_load_comments,
            "loadNewsComment",
            false
        )
    }
}