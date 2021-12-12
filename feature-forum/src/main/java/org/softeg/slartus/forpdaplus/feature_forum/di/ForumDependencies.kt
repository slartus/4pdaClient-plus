package org.softeg.slartus.forpdaplus.feature_forum.di

import org.softeg.slartus.forpdaplus.feature_forum.entities.ForumItem

interface ForumDependencies {
    val forumsService: ForumService
    val forumsDb: ForumDb
    val forumPreferences: ForumPreferences

    fun showForumTopicsList(forumId: String?, forumTitle: String?)
}

interface ForumPreferences {
    fun setStartForum(id: String?, title: String?)

    val showImages: Boolean
    val startForumId: String?
}

interface ForumService {
    suspend fun getGithubForum(): List<ForumItem>
    suspend fun getSlartusForum(): List<ForumItem>
    fun markAsRead(forumId: String)
}

interface ForumDb {
    suspend fun getAll(): List<ForumItem>
    suspend fun merge(forums: List<ForumItem>)
}