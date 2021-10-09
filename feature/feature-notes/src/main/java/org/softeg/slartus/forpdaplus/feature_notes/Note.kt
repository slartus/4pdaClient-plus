package org.softeg.slartus.forpdaplus.feature_notes

import java.util.*

data class Note(
    val id: Int? = null,
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