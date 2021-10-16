package org.softeg.slartus.forpdaplus.features.feature_notes

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.softeg.slartus.forpdaplus.CustomTestRunner
import org.softeg.slartus.forpdaplus.RecyclerViewItemCountAssertion
import org.softeg.slartus.forpdaplus.feature_notes.Note
import org.softeg.slartus.forpdaplus.feature_notes.NotesDao
import org.softeg.slartus.forpdaplus.feature_notes.R
import org.softeg.slartus.forpdaplus.feature_notes.ui.list.NotesListFragment
import org.softeg.slartus.forpdaplus.launchFragmentInHiltContainer
import java.util.*
import javax.inject.Inject

@HiltAndroidTest
class NoteTests {

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
        Espresso.onView(ViewMatchers.withId(R.id.list))
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
        Espresso.onView(ViewMatchers.withText(R.string.delete)).perform(ViewActions.click())
        Thread.sleep(300)
        Espresso.onView(ViewMatchers.withText(R.string.cancel)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.list))
            .check(RecyclerViewItemCountAssertion(1))

        // delete test
        showContextMenu(0)
        Espresso.onView(ViewMatchers.withText(R.string.delete)).perform(ViewActions.click())
        Thread.sleep(300)
        Espresso.onView(ViewMatchers.withText(R.string.delete)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.list))
            .check(RecyclerViewItemCountAssertion(0))
    }

    @Test
    fun listItemContextLinksTest() {
        launchFragmentInHiltContainer<NotesListFragment>()
        addNoteToList(Note(title = "title $1", date = Date()))

        // check links menu not exists
        showContextMenu(0)
        Espresso.onView(ViewMatchers.withText(R.string.links))
            .check(ViewAssertions.doesNotExist())
        Espresso.pressBack()

        addNoteToList(Note(url = "someUrl", date = Date()))
        showContextMenu(0)
        Espresso.onView(ViewMatchers.withText(R.string.links)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.link_to_post))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.pressBack()

        addNoteToList(Note(topicId = "someId", topicTitle = "some title", date = Date()))
        showContextMenu(0)
        Espresso.onView(ViewMatchers.withText(R.string.links)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("some title"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.pressBack()

        addNoteToList(Note(userId = "someId", userName = "some name", date = Date()))
        showContextMenu(0)
        Espresso.onView(ViewMatchers.withText(R.string.links)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("some name"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.pressBack()
    }

    private fun addItemsAndCheckCount(itemsCount: Int) {
        runBlocking {
            List(itemsCount) { it }.forEach {
                notesDao.insert(Note(title = "title ${it + 1}", body = "body $it", date = Date()))
            }
            delay(300)
        }

        Espresso.onView(ViewMatchers.withId(R.id.list))
            .check(RecyclerViewItemCountAssertion(itemsCount))
    }

    private fun addNoteToList(note: Note) {
        runBlocking {
            notesDao.insert(note)
            delay(300)
        }
    }

    private fun showContextMenu(position: Int) {
        Espresso.onView(ViewMatchers.withId(R.id.list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    position,
                    ViewActions.longClick()
                )
            )
    }
}