package org.softeg.slartus.forpdaplus.forum.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class ForumDao {
    @Transaction
    open suspend fun replaceAll(forums: List<ForumEntity>) {
        deleteAll()
        insertAll(*forums.toTypedArray())
    }

    @Query("SELECT * FROM forum")
    abstract suspend fun getAll(): List<ForumEntity>

    @Insert
    abstract suspend fun insertAll(vararg items: ForumEntity)

    @Query("DELETE FROM forum")
    abstract suspend fun deleteAll()
}