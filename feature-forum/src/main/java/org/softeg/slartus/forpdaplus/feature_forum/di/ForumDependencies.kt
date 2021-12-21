package org.softeg.slartus.forpdaplus.feature_forum.di

import org.softeg.slartus.forpdaplus.feature_forum.entities.ForumItem

interface ForumDependencies {
    val forumsService: ForumService
    val forumsDb: ForumDb
    fun showForumTopicsList(forumId: String?, forumTitle: String?)
}

interface ForumService {
    suspend fun getGithubForum(): List<ForumItem>
    suspend fun getSlartusForum(): List<ForumItem>
    fun markAsRead(forumId: String)
    fun getForumUrl(forumId: String?): String
}

interface ForumDb {
    suspend fun getAll(): List<ForumItem>
    suspend fun merge(forums: List<ForumItem>)
}