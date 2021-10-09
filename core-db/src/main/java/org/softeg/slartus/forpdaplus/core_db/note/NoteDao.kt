package org.softeg.slartus.forpdaplus.core_db.note

import androidx.room.*

@Dao
abstract class NoteDao {
    //https://medium.com/androiddevelopers/room-coroutines-422b786dc4c5
//    @Transaction
//    open suspend fun setLoggedInUser(loggedInUser: User) {
//        deleteUser(loggedInUser)
//        insertUser(loggedInUser)
//    }
    @Query("SELECT * FROM note")
    abstract suspend fun getAll(): List<Note>

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
}