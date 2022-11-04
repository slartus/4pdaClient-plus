package org.softeg.slartus.forpdaplus.forum.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.forum.data.db.ForumDao
import org.softeg.slartus.forpdaplus.forum.data.db.mapToDb
import org.softeg.slartus.forpdaplus.forum.data.db.mapToItem
import ru.softeg.slartus.forum.api.ForumItem
import javax.inject.Inject

class LocalForumDataSource @Inject constructor(private val forumDao: ForumDao) {
    suspend fun getAll(): List<ForumItem> = withContext(Dispatchers.IO) {
        forumDao.getAll().map { it.mapToItem() }
    }

    suspend fun merge(forums: List<ForumItem>) = withContext(Dispatchers.IO) {
        forumDao.replaceAll(forums.map { it.mapToDb() })
    }
}