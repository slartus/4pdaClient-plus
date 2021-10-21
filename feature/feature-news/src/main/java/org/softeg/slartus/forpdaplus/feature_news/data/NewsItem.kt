package org.softeg.slartus.forpdaplus.feature_news.data

import java.util.*

data class NewsListItem(
    val id: String?,
    val url: String?,
    val title: String?,
    val description: String?,
    val authorId: String?,
    val author: String?,
    val date: Date?,
    val imgUrl: String?,
    val commentsCount: Int?,
)