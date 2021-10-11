package org.softeg.slartus.forpdaplus.core_db.note

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val title: String? = null,
    val body: String? = null,
    val url: String? = null,
    val topicId: String? = null,
    val topicTitle: String? = null,
    val postId: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val date: Date
)