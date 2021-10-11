package org.softeg.slartus.forpdaplus.core_db.tests

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
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
    fun closeDb() {
        runBlocking {
            db.noteDao().deleteAll()
        }

        db.close()
    }

    @Test
    fun insertAndReadInList() {
        val item = Note(id = 1, date = Date(), title = "some title")
        runBlocking {
            db.noteDao().insert(item)

            val dbItem = db.noteDao()
                .getAll().first()
            assertThat(item.toString(), equalTo(dbItem.toString()))
        }

    }

    @Test
    fun insertAndDelete() {
        runBlocking {
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

    @Test
    fun getByTopicId() {
        runBlocking {
            db.noteDao().insert(Note(date = Date(), topicId = null))
            db.noteDao().insert(Note(date = Date(), topicId = "2"))
            db.noteDao().insert(Note(date = Date(), topicId = "3"))
            db.noteDao().insert(Note(date = Date(), topicId = "3"))

            assertThat(
                db.noteDao()
                    .getAll().size, equalTo(4)
            )

            assertThat(
                db.noteDao()
                    .getByTopicId("2").size, equalTo(1)
            )
            assertThat(
                db.noteDao()
                    .getByTopicId("2").first().topicId, equalTo("2")
            )

            assertThat(
                db.noteDao()
                    .getByTopicId("3").size, equalTo(2)
            )

            assertThat(
                db.noteDao()
                    .getByTopicId("4").size, equalTo(0)
            )
        }
    }

    @Test
    fun getNoteById() {
        runBlocking {
            db.noteDao().insert(Note(id = 1, date = Date(), topicId = null))

            assertThat(db.noteDao().get(1)?.id, equalTo(1))
            assertThat(db.noteDao().get(5)?.id, equalTo(null))
        }
    }

    @Test
    fun update() {
        runBlocking {

            getNoteTestVariants().forEachIndexed { index, note ->
                val dbNote = Note(id = index, date = Date())
                db.noteDao().insert(dbNote)
                val updateNote = note.copy(id = index)
                db.noteDao().update(updateNote)

                assertThat(db.noteDao().get(index)?.toString(), equalTo(updateNote.toString()))
            }
        }
    }

    @Test
    fun deleteById() {
        runBlocking {
            db.noteDao().insert(Note( date = Date()))
            db.noteDao().insert(Note(id = 1, date = Date()))
            db.noteDao().insert(Note(id = 2, date = Date()))
            db.noteDao().delete(1)

            assertThat( db.noteDao().getAll().size, equalTo(2))
        }
    }

    private fun getNoteTestVariants() =
        listOf(
            Note(date = Date()),
            Note(date = Date(), title = "test title"),
            Note(date = Date(), body = "test body"),
            Note(date = Date(), url = "test url"),
            Note(date = Date(), topicId = "666"),
            Note(date = Date(), topicTitle = "test topicTitle"),
            Note(date = Date(), postId = "13"),
            Note(date = Date(), userId = "254"),
            Note(date = Date(), userName = "test userName")
        )

}