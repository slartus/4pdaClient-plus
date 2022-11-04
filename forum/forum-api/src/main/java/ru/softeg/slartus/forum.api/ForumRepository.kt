package ru.softeg.slartus.forum.api

import kotlinx.coroutines.flow.Flow

interface ForumRepository {
    val forum: Flow<List<ForumItem>>
    suspend fun load()
    suspend fun markAsRead(forumId: String)
    fun getForumUrl(forumId: String?): String
}

