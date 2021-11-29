package org.softeg.slartus.forpdaplus.feature_forum.repository

import kotlinx.coroutines.flow.MutableStateFlow
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumDb
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumService
import org.softeg.slartus.forpdaplus.feature_forum.entity.Forum
import javax.inject.Inject

class ForumRepositoryImpl @Inject constructor(
    private val forumService: ForumService,
    private val forumDb: ForumDb
) : ForumRepository {

    private val _forum = MutableStateFlow<List<Forum>>(emptyList())
    override val forum
        get() = _forum

    override suspend fun load() {
        _forum.value = forumDb.getAll()
        val forums = try {
            forumService.getGithubForum()
        } catch (ex: Throwable) {
            forumService.getSlartusForum()
        }
        forumDb.merge(forums)
        _forum.value = forumDb.getAll()
    }

    override suspend fun getAll(): List<Forum> {
        return forumDb.getAll()
    }
}