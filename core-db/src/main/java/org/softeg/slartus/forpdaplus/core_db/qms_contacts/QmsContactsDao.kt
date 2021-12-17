package org.softeg.slartus.forpdaplus.core_db.qms_contacts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class QmsContactsDao {

    @Query("SELECT * FROM qms_contacts ORDER BY `order`")
    abstract suspend fun getAll(): List<QmsContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(vararg items: QmsContact)

    @Query("DELETE FROM qms_contacts")
    abstract suspend fun deleteAll()
}