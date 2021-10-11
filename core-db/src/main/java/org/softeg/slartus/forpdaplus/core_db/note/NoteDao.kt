package org.softeg.slartus.forpdaplus.core_db.note

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NoteDao {
    //https://medium.com/androiddevelopers/room-coroutines-422b786dc4c5
    @Transaction
    open suspend fun merge(notes: List<Note>) {
        deleteAll()
        insertAll(*notes.toTypedArray())
    }

    @Query("SELECT * FROM note")
    abstract fun getAll(): Flow<List<Note>>

    @Query("SELECT * FROM note where topicId=:topicId")
    abstract suspend fun getByTopicId(topicId: String): List<Note>

    @Query("SELECT * FROM note where id=:id")
    abstract suspend fun get(id: Int): Note?

    @Insert
    abstract suspend fun insert(note: Note)

    @Update
    abstract suspend fun update(note: Note)

    @Insert
    abstract suspend fun insertAll(vararg notes: Note)

    @Query("DELETE FROM note")
    abstract suspend fun deleteAll()

    @Delete
    abstract suspend fun delete(note: Note)

    @Query("DELETE FROM note WHERE id = :id")
    abstract suspend fun delete(id: Int)

}