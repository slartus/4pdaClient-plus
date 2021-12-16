package org.softeg.slartus.forpdaplus.domain_forum

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdaplus.core.db.ForumTable
import org.softeg.slartus.forpdaplus.core.entities.Forum
import org.softeg.slartus.forpdaplus.core.repositories.ForumRepository
import org.softeg.slartus.forpdaplus.core.services.ForumService
import javax.inject.Inject

class ForumRepositoryImpl @Inject constructor(
    private val forumService: ForumService,
    private val forumTable: ForumTable
) : ForumRepository {

    private val _forum = MutableStateFlow<List<Forum>>(emptyList())
    override val forum
        get() = _forum.asStateFlow()

    override suspend fun load() {
        _forum.value = forumTable.getAll()

        val forums = try {
            forumService.getGithubForum()
        } catch (ex: Throwable) {
            forumService.getSlartusForum()
        }
        forumTable.merge(forums)

        _forum.value = forumTable.getAll()
    }

    override suspend fun markAsRead(forumId: String) {
        forumService.markAsRead(forumId)
    }

    override fun getForumUrl(forumId: String?): String {
        return forumService.getForumUrl(forumId)
    }

}