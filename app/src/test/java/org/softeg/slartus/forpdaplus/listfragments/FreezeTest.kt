package org.softeg.slartus.forpdaplus.listfragments


import android.os.Build
import android.os.Looper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.softeg.slartus.forpdaplus.MainActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.contrib.DrawerActions
import org.softeg.slartus.forpdaplus.R
import androidx.test.filters.LargeTest
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(AndroidJUnit4::class)
@LargeTest
class FreezeTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity>
            = ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun click_item(){
        activityRule.scenario.onActivity {
            // Open Drawer to click on navigation.
            onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
            shadowOf(Looper.getMainLooper()).idle()
        }
    }
}