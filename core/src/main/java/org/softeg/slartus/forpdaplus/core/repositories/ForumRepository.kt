package org.softeg.slartus.forpdaplus.core.repositories

import kotlinx.coroutines.flow.Flow
import ru.softeg.slartus.forum.api.Forum

interface ForumRepository {
    val forum: Flow<List<Forum>>
    suspend fun load()
    suspend fun markAsRead(forumId: String)
    fun getForumUrl(forumId: String?): String
}

