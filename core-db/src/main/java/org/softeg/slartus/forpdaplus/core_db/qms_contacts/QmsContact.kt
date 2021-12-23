package org.softeg.slartus.forpdaplus.core_db.qms_contacts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qms_contacts")
data class QmsContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val nick: String? = null,
    val avatarUrl: String? = null,
    val messagesCount: Int? = null,
    val sort: Int? = null
)