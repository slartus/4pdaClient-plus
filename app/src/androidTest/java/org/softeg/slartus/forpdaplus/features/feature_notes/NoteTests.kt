package org.softeg.slartus.forpdaplus.features.feature_notes

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
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
import org.softeg.slartus.forpdaplus.feature_notes.ui.note.NoteFragment
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
        runFragment()
        val note = notesList().random()
        runBlocking {
            notesDao.insert(note)
        }
withId(infoTable)
    }

    private fun runFragment() = launchFragmentInHiltContainer<NoteFragment>()
}