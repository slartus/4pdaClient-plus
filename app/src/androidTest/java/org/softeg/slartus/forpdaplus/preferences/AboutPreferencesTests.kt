package org.softeg.slartus.forpdaplus.preferences

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
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
class AboutPreferencesTests {
    @get:Rule
    var activityRule: ActivityScenarioRule<PreferencesActivity> =
        ActivityScenarioRule(PreferencesActivity::class.java)

    private val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun aboutTest() {
        appearanceClick(R.string.about)

        appearanceClick(R.string.about)
        onView(withText(R.string.ok)).perform(click())

        appearanceClick(R.string.ChangesHistory)
        onView(withText(R.string.ok)).perform(click())

        appearanceAction(R.string.share_it, nothing())
        appearanceAction(R.string.open_topic, nothing())
        appearanceAction(R.string.check_new_version, nothing())
        appearanceAction(R.string.alternative_client_radiation, nothing())
    }
}