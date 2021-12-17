package org.softeg.slartus.forpdaplus.core_db.forum

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forum")
data class Forum(
    @PrimaryKey(autoGenerate = true)
    val _id: Int? = null,
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val isHasTopics: Boolean,
    val isHasForums: Boolean,
    val iconUrl: String? = null,
    val parentId: String? = null
)