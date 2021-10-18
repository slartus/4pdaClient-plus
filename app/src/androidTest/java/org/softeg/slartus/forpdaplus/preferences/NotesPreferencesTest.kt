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
class NotesPreferencesTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<PreferencesActivity> =
        ActivityScenarioRule(PreferencesActivity::class.java)

    private val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun test() {
        appearanceClick(R.string.notes)

        listPreferenceTest(
            context,
            R.string.notes_placement,
            "notes.placement",
            R.array.NotesStoragePlacements,
            R.array.NotesStoragePlacementValues,
            "local"
        )

    }
}