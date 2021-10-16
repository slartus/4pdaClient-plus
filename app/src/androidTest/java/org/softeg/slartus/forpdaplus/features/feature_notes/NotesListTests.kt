package org.softeg.slartus.forpdaplus.features.feature_notes

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.softeg.slartus.forpdaplus.feature_notes.Note
import org.softeg.slartus.forpdaplus.feature_notes.NotesDao
import org.softeg.slartus.forpdaplus.feature_notes.ui.list.NotesListFragment
import org.softeg.slartus.forpdaplus.launchFragmentInHiltContainer
import org.softeg.slartus.forpdaplus.RecyclerViewItemCountAssertion
import org.softeg.slartus.forpdaplus.feature_notes.R
import org.softeg.slartus.forpdaplus.feature_notes.data.topicUrl
import org.softeg.slartus.forpdaplus.feature_notes.data.userUrl
import java.util.*
import javax.inject.Inject
import org.softeg.slartus.forpdaplus.ApplicationTestRule
import org.softeg.slartus.forpdaplus.CustomTestRunner

@HiltAndroidTest
class NotesListTests {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var notesDao: NotesDao

    init {
        CustomTestRunner.hiltTest = true
    }

    @Before
    fun init() {

        hiltRule.inject()
        runBlocking {
            notesDao.deleteAll()
        }
    }

    @Test
    fun listReactionTest() {
        launchFragmentInHiltContainer<NotesListFragment>()
        onView(withId(R.id.list))
            .check(RecyclerViewItemCountAssertion(0))

        val itemsCount = 200
        addItemsAndCheckCount(itemsCount)
    }

    @Test
    fun listItemContextDeleteTest() {
        launchFragmentInHiltContainer<NotesListFragment>()
        runBlocking {
            notesDao.insert(Note(title = "title $1", body = "body $1", date = Date()))
            delay(300)
        }

        // delete cancel test
        showContextMenu(0)
        onView(withText(R.string.delete)).perform(click())
        Thread.sleep(300)
        onView(withText(R.string.cancel)).perform(click())
        onView(withId(R.id.list))
            .check(RecyclerViewItemCountAssertion(1))

        // delete test
        showContextMenu(0)
        onView(withText(R.string.delete)).perform(click())
        Thread.sleep(300)
        onView(withText(R.string.delete)).perform(click())
        onView(withId(R.id.list))
            .check(RecyclerViewItemCountAssertion(0))
    }

    @Test
    fun listItemContextLinksTest() {
        launchFragmentInHiltContainer<NotesListFragment>()
        addNoteToList(Note(title = "title 1", date = Date()))

        // check links menu not exists
        showContextMenu(0)
        onView(withText(R.string.links)).check(doesNotExist())
        Espresso.pressBack()

        addNoteToList(Note(url = "someUrl", date = Date()))
        showContextMenu(0)
        onView(withText(R.string.links)).perform(click())
        onView(withText(R.string.link_to_post)).check(matches(isDisplayed()))
        Espresso.pressBack()

        addNoteToList(Note(topicId = "someId", topicTitle = "some title", date = Date()))
        showContextMenu(0)
        onView(withText(R.string.links)).perform(click())
        onView(withText("some title")).check(matches(isDisplayed()))
        Espresso.pressBack()

        addNoteToList(Note(userId = "someId", userName = "some name", date = Date()))
        showContextMenu(0)
        onView(withText(R.string.links)).perform(click())
        onView(withText("some name")).check(matches(isDisplayed()))
        Espresso.pressBack()
    }

    @Test
    fun saveRestoreStateTest() {
        val scenario = launchFragmentInHiltContainer<NotesListFragment>()
        addItemsAndCheckCount(200)

        onView(withId(R.id.list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    100,
                    longClick()
                )
            )
        scenario.recreate()

        onView(withText("title 100"))
            .check(matches(isDisplayed()))
    }

    private fun addItemsAndCheckCount(itemsCount: Int) {
        runBlocking {
            List(itemsCount) { it }.forEach {
                notesDao.insert(Note(title = "title ${it + 1}", body = "body $it", date = Date()))
            }
            delay(300)
        }

        onView(withId(R.id.list))
            .check(RecyclerViewItemCountAssertion(itemsCount))
    }

    private fun addNoteToList(note: Note) {
        runBlocking {
            notesDao.insert(note)
            delay(300)
        }
    }

    private fun showContextMenu(position: Int) {
        onView(withId(R.id.list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    position,
                    longClick()
                )
            )
    }
    companion object{

    }
}