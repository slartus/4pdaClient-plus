package org.softeg.slartus.forpdaplus

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
class PreferencesScreenTests {
    @get:Rule
    var activityRule: ActivityScenarioRule<PreferencesActivity> =
        ActivityScenarioRule(PreferencesActivity::class.java)

    @Test
    fun test() {
        val testContext: Context = getInstrumentation().targetContext
        // val scenario = launchFragmentInContainer<PreferencesActivity.PrefsFragment>()
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.appearance)), click()
                )
            )

        testContext.resources
            .getStringArray(R.array.appthemesArray)
            .forEach {
                chooseThemeTest(it)
            }

        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.pick_color_with_pencil)), click()
                )
            )
        onView(withText(R.string.accept)).perform(click())

        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.pick_accent_color)), click()
                )
            )
        onView(withText(R.string.accept)).perform(click())

        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.webview_font)), click()
                )
            )
        onView(withText(R.string.system_font)).perform(click())
        onView(withText(R.string.accept)).perform(click())

        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.user_background_title)), click()
                )
            )
        onView(withText(R.string.reset)).perform(click())
    }

    private fun chooseThemeTest(themeNameResId: String) {
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.theme)), click()
                )
            )
        onView(withText(themeNameResId)).perform(click())
        onView(withText(R.string.AcceptStyle)).perform(click())
    }
}