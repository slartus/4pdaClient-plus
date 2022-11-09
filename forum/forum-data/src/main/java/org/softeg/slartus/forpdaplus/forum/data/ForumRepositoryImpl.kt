package org.softeg.slartus.forpdaplus.forum.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.softeg.slartus.forum.api.ForumItem
import ru.softeg.slartus.forum.api.ForumRepository
import ru.softeg.slartus.forum.api.ForumService
import javax.inject.Inject

class ForumRepositoryImpl @Inject constructor(
    private val forumService: ForumService,
    private val forumTable: LocalForumDataSource,
    private val assetsForumDataSource: AssetsForumDataSource
) : ForumRepository {

    private val _forum = MutableStateFlow<List<ForumItem>>(emptyList())
    override val forum
        get() = _forum.asStateFlow()

    override suspend fun load() {
        _forum.value = forumTable.getAll()

        val forums =
            kotlin.runCatching {
                forumService.getGithubForum()
            }.onFailure {
                it.printStackTrace()
            }.getOrNull() ?: kotlin.runCatching {
                forumService.getSlartusForum()
            }.onFailure {
                it.printStackTrace()
            }.getOrNull() ?: emptyList()

        if (forums.isNotEmpty()) {
            forumTable.merge(forums)
        }

        _forum.value = forumTable.getAll().getNullIfEmpty() ?: assetsForumDataSource.getAll()
    }

    override suspend fun markAsRead(forumId: String) {
        forumService.markAsRead(forumId)
    }

    override fun getForumUrl(forumId: String?): String {
        return forumService.getForumUrl(forumId)
    }

}

fun <T> List<T>?.getNullIfEmpty(): List<T>? {
    return if (this?.isEmpty() == true) null else this
}