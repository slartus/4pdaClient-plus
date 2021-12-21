package org.softeg.slartus.forpdaplus.core.repositories

import kotlinx.coroutines.flow.Flow
import org.softeg.slartus.forpdaplus.core.entities.Forum

interface ForumRepository {
    val forum: Flow<List<Forum>>
    suspend fun load()
    suspend fun markAsRead(forumId: String)
    fun getForumUrl(forumId: String?): String
}

