package org.softeg.slartus.forpdaplus.feature_forum.di

import org.softeg.slartus.forpdaplus.feature_forum.entity.Forum

interface ForumDependencies {
    val forumsService: ForumService
    val forumsDb: ForumDb
}

interface ForumService {
    suspend fun getGithubForum(): List<Forum>
    suspend fun getSlartusForum(): List<Forum>
}

interface ForumDb {
    suspend fun getAll(): List<Forum>
    suspend fun merge(forums: List<Forum>)
}