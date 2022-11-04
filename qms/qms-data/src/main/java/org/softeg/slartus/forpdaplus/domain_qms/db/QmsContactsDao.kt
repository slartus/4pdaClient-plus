package org.softeg.slartus.forpdaplus.domain_qms.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QmsContactsDao {
    @Query("SELECT * FROM qms_contacts ORDER BY `sort`")
    suspend fun getAll(): List<QmsContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg items: QmsContactEntity)

    @Query("DELETE FROM qms_contacts")
    suspend fun deleteAll()

    @Query("SELECT * FROM qms_contacts WHERE id=:id")
    suspend fun findById(id: Long): QmsContactEntity?
}