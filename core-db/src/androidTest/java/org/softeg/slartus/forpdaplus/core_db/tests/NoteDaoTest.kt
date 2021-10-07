package org.softeg.slartus.forpdaplus.core_db.tests

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.softeg.slartus.forpdaplus.core_db.AppDatabase
import org.softeg.slartus.forpdaplus.core_db.note.Note
import java.io.IOException
import java.util.*
import javax.inject.Inject

@HiltAndroidTest
class NoteDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: AppDatabase

    @Before
    fun init() {
        hiltRule.inject()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.noteDao().deleteAll()
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndReadInList() {
        val item = Note(id = 1, date = Date(), title = "some title")
        db.noteDao().insert(item)

        val dbItem = db.noteDao()
            .getAll().first()
        assertThat(item.toString(), equalTo(dbItem.toString()))
    }

    @Test
    @Throws(Exception::class)
    fun insertAndDelete() {
        val item = Note(date = Date())
        db.noteDao().insert(item)
        val dbItem = db.noteDao()
            .getAll().first()
        db.noteDao().delete(dbItem)
        assert(
            db.noteDao()
                .getAll().isEmpty()
        )
    }
}