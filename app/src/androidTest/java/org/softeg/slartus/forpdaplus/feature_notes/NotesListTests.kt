package org.softeg.slartus.forpdaplus.feature_notes

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.github.terrakok.cicerone.NavigatorHolder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.feature_notes.ui.list.NotesListFragment
import org.softeg.slartus.forpdaplus.launchFragmentInHiltContainer
import org.softeg.slartus.forpdaplus.navigation.MainActivityNavigator
import org.softeg.slartus.forpdaplus.preferences.RecyclerViewItemCountAssertion
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class ListPreferencesTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun init() {
        hiltRule.inject()

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.clear()
        editor.apply()
    }

    private val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun test() {
//        val scenario: ActivityScenario<TestActivity> = startActivity()
//        scenario.onActivity { activity: TestActivity ->
//
//        }
        onView(isRoot()).perform(ViewActions.pressMenuKey());
    }

    @Test
    fun testEventFragment() {
        val scenario = launchFragmentInContainer<NotesListFragment>()
        onView(withText("some")).perform(click())
        // Assert some expected behavior

    }
}

@HiltAndroidTest
class FooTest {
    //  @Rule public HiltAndroidRule hiltRule = new HiltAndroidRule(this);
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var notesDao: NotesDao

    @Before
    fun init() {
        hiltRule.inject()
        runBlocking {
            notesDao.deleteAll()
        }

    }

    @Test
    fun test() {
        launchFragmentInHiltContainer<NotesListFragment>()
        onView(withId(R.id.list))
            .check(RecyclerViewItemCountAssertion(0))
        Thread.sleep(5000)
        runBlocking {
            notesDao.insert(Note(date = Date()))

        }
        Thread.sleep(5000)
        onView(withId(R.id.list))
            .check(RecyclerViewItemCountAssertion(1))

    }
}