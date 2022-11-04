package ru.softeg.slartus.forum.api

import ru.softeg.slartus.forum.api.ForumItem

interface ForumService {
    suspend fun getGithubForum(): List<ForumItem>
    suspend fun getSlartusForum(): List<ForumItem>
    suspend fun markAsRead(forumId: String)
    fun getForumUrl(forumId: String?): String
}