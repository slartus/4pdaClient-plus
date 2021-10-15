package org.softeg.slartus.forpdaplus.features.feature_notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.softeg.slartus.forpdaplus.feature_notes.Note
import org.softeg.slartus.forpdaplus.feature_notes.NotesDao
import org.softeg.slartus.forpdaplus.feature_notes.ui.list.NotesListFragment
import org.softeg.slartus.forpdaplus.launchFragmentInHiltContainer
import org.softeg.slartus.forpdaplus.preferences.RecyclerViewItemCountAssertion
import org.softeg.slartus.forpdaplus.feature_notes.R
import java.util.*
import javax.inject.Inject

@HiltAndroidTest
class NotesListTests {
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