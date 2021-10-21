package org.softeg.slartus.forpdaplus.core_api.model

import java.util.*

data class ApiNewsListItem(
    val id: String?,
    val url: String?,
    val title: String?,
    val description: String?,
    val authorId: String?,
    val author: String?,
    val date: Date?,
    val imgUrl: String?,
    val commentsCount: Int?,
    val avatar: String?,
    val tags: List<Tag>
)

data class Tag(val title: String)