package org.softeg.slartus.forpdaplus.core_db.note

import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    fun getAll(): List<Note>

    @Insert
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Insert
    fun insertAll(vararg notes: Note)

    @Query("DELETE FROM note")
    fun deleteAll()

    @Delete
    fun delete(note: Note)
}