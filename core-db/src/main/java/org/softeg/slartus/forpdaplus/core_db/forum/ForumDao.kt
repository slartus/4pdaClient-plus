package org.softeg.slartus.forpdaplus.core_db.forum

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class ForumDao {
    //https://medium.com/androiddevelopers/room-coroutines-422b786dc4c5
    @Transaction
    open suspend fun merge(forums: List<Forum>) {
        deleteAll()
        insertAll(*forums.toTypedArray())
    }

    @Query("SELECT * FROM forum")
    abstract suspend fun getAll(): List<Forum>

    @Insert
    abstract suspend fun insertAll(vararg items: Forum)

    @Query("DELETE FROM forum")
    abstract suspend fun deleteAll()
}