package org.softeg.slartus.forpdaplus.core_db.forum

import org.softeg.slartus.forpdaplus.core.db.ForumTable
import org.softeg.slartus.forpdaplus.core.entities.Forum
import javax.inject.Inject
import org.softeg.slartus.forpdaplus.core_db.forum.Forum as ForumDbItem

class ForumTableImpl @Inject constructor(private val forumDao: ForumDao) : ForumTable {
    override suspend fun getAll(): List<Forum> {
        return forumDao.getAll().map { it.mapToItem() }

    }

    override suspend fun merge(forums: List<Forum>) {
        return forumDao.merge(forums.map { it.mapToDb() })
    }

    private data class ForumItem(
        override val id: String?,
        override val title: String?,
        override val description: String?,
        override val isHasTopics: Boolean,
        override val isHasForums: Boolean,
        override val iconUrl: String?,
        override val parentId: String?
    ) : Forum

    companion object {
        private fun ForumDbItem.mapToItem() = ForumItem(
            this.id,
            this.title,
            this.description,
            this.isHasTopics,
            this.isHasForums,
            this.iconUrl,
            this.parentId
        )

        private fun Forum.mapToDb() = ForumDbItem(null,
            this.id,
            this.title,
            this.description,
            this.isHasTopics,
            this.isHasForums,
            this.iconUrl,
            this.parentId
        )
    }
}