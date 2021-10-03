package org.softeg.slartus.forpdaplus.preferences

import android.content.Context
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.hamcrest.CoreMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
class TopicViewPreferencesTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<PreferencesActivity> =
        ActivityScenarioRule(PreferencesActivity::class.java)

    private val context: Context by lazy {
        getInstrumentation().targetContext
    }

    @Test
    fun test() {
        appearanceClick(R.string.topic_view)
        checkBoxPreferenceTest(
            context,
            R.string.spoil_first_post_title,
            "theme.SpoilFirstPost",
            true
        )

        checkBoxPreferenceTest(
            context,
            R.string.prefs_confirm_send,
            "theme.ConfirmSend",
            true
        )

        listPreferenceTest(
            context,
            R.string.prefs_load_images,
            "theme.LoadsImages",
            R.array.ConnectTypeArray,
            R.array.ConnectTypeValues,
            "1"
        )

        checkBoxPreferenceTest(
            context,
            R.string.prefs_use_volumes_scroll,
            "theme.UseVolumesScroll",
            false
        )

        checkBoxPreferenceTest(
            context,
            R.string.prefs_keep_screen_on,
            "theme.KeepScreenOn",
            false
        )

        checkBoxPreferenceTest(
            context,
            R.string.prefs_spoiler_by_button,
            "theme.SpoilerByButton",
            false
        )

        checkBoxPreferenceTest(
            context,
            R.string.prefs_show_readers_writers,
            "theme.ShowReadersAndWriters",
            false
        )

        downloadingFilesTest()

        checkBoxPreferenceTest(
            context,
            R.string.prefs_full_topic_title,
            "fullThemeTitle",
            false
        )
    }

    private fun downloadingFilesTest(){
        appearanceClick(R.string.prefs_files_downloading)
        checkBoxPreferenceTest(
            context,
            R.string.prefs_confirm_download_file,
            "files.ConfirmDownload",
            true
        )
        listPreferenceTest(
            context,
            R.string.prefs_download_managers,
            "file.downloaderManagers",
            R.array.downloaderManagersArray,
            R.array.downloaderManagersValues,
            null
        )
        pressBack()
    }

}