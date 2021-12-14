package org.softeg.slartus.forpdaplus.feature_forum.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.entities.Forum
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumDb
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumService
import javax.inject.Inject

class ForumRepositoryImpl @Inject constructor(
    private val forumService: ForumService,
    private val forumDb: ForumDb
) : org.softeg.slartus.forpdaplus.core.repositories.ForumRepository {

    private val _forum = MutableStateFlow<List<Forum>>(emptyList())
    override val forum
        get() = _forum

    override suspend fun load() {
        withContext(Dispatchers.Main) {
            _forum.value = forumDb.getAll()
        }
        withContext(Dispatchers.IO) {
            val forums = try {
                forumService.getGithubForum()
            } catch (ex: Throwable) {
                forumService.getSlartusForum()
            }
            forumDb.merge(forums)
        }
        withContext(Dispatchers.Main) {
            _forum.value = forumDb.getAll()
        }
    }

    override suspend fun markAsRead(forumId: String) {
        withContext(Dispatchers.IO) {
            forumService.markAsRead(forumId)
        }
    }

    override fun getForumUrl(forumId: String?): String {
        return forumService.getForumUrl(forumId)
    }

}