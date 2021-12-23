package org.softeg.slartus.forpdaplus.core.services

import org.softeg.slartus.forpdaplus.core.entities.Forum

interface ForumService {
    suspend fun getGithubForum(): List<Forum>
    suspend fun getSlartusForum(): List<Forum>
    suspend fun markAsRead(forumId: String)
    fun getForumUrl(forumId: String?): String
}