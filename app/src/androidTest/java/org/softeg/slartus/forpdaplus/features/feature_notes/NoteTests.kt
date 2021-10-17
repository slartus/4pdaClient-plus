package org.softeg.slartus.forpdaplus.features.feature_notes

import android.os.Bundle
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.softeg.slartus.forpdaplus.CustomTestRunner
import org.softeg.slartus.forpdaplus.feature_notes.Note
import org.softeg.slartus.forpdaplus.feature_notes.NotesDao
import org.softeg.slartus.forpdaplus.feature_notes.ui.note.NoteFragment
import org.softeg.slartus.forpdaplus.launchFragmentInHiltContainer
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
        val note = notesList().random()
        var dbNote: Note? = null
        runBlocking {
            notesDao.insert(note)
            notesDao.getAllFlow().take(1).collect {
                dbNote = it.first()
            }
        }
        runFragment(dbNote?.id ?: 0)

    }

    private fun runFragment(noteId: Int) = launchFragmentInHiltContainer<NoteFragment>(
        fragmentArgs = Bundle().apply { putInt("NoteFragment.NOTE_ID", noteId) }
    )
}