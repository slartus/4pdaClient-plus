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
class ListPreferencesTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<PreferencesActivity> =
        ActivityScenarioRule(PreferencesActivity::class.java)

    private val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun test() {
        appearanceClick(R.string.lists)

        checkBoxPreferenceTest(
            context,
            R.string.scroll_by_buttons,
            "lists.scroll_by_buttons",
            false
        )

        checkBoxPreferenceTest(
            context,
            R.string.refresh_list,
            "lists.refresh",
            true
        )

        checkBoxPreferenceTest(
            context,
            R.string.refresh_tab_list,
            "lists.refresh_on_tab",
            false
        )

        checkBoxPreferenceTest(
            context,
            R.string.hide_description_in_item,
            "showSubMain",
            false
        )

        checkBoxPreferenceTest(
            context,
            R.string.load_all,
            "lists.favorites.load_all",
            false
        )

        checkBoxPreferenceTest(
            context,
            R.string.show_images,
            "forum.list.show_images",
            true
        )

        listPreferenceTest(
            context,
            R.string.menu_news_load_images,
            "news.list.loadimages",
            R.array.ConnectTypeArray,
            R.array.ConnectTypeValues,
            "1"
        )

        listPreferenceTest(
            context,
            R.string.list_view,
            "news.list.view",
            R.array.NewsListViewTitles,
            R.array.NewsListViewValues,
            "full"
        )
    }
}