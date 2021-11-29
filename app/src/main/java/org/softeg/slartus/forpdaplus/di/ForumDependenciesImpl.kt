package org.softeg.slartus.forpdaplus.di

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.softeg.slartus.forpdaplus.core_db.forum.ForumDao
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumDb
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumDependencies
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumService
import ru.slartus.http.Http
import javax.inject.Inject
import org.softeg.slartus.forpdaplus.feature_forum.entity.Forum as FeatureForum
import org.softeg.slartus.forpdaplus.core_db.forum.Forum as DbForum

class ForumDependenciesImpl @Inject constructor(
    override val forumsService: ForumService,
    override val forumsDb: ForumDb
) : ForumDependencies

class ForumServiceImpl @Inject constructor(): ForumService {
    override suspend fun getGithubForum(): List<FeatureForum> {
        val response = Http.instance
            .performGet("https://raw.githubusercontent.com/slartus/4pdaClient-plus/master/forum_struct.json")

        val itemsListType = object : TypeToken<List<FeatureForum>>() {}.type
        return Gson().fromJson(response.responseBody, itemsListType)
    }

    override suspend fun getSlartusForum(): List<FeatureForum> {
        val response = Http.instance
            .performGet("http://slartus.ru/4pda/forum_struct.json")

        val itemsListType = object : TypeToken<List<FeatureForum>>() {}.type
        return Gson().fromJson(response.responseBody, itemsListType)
    }
}

class ForumDbImpl @Inject constructor(
    private val forumDao: ForumDao
) : ForumDb {
    override suspend fun getAll(): List<FeatureForum> {
        return forumDao.getAll().map { it.map() }
    }

    override suspend fun merge(forums: List<FeatureForum>) {
        return forumDao.merge(forums.map { it.map() })
    }
}

private fun DbForum.map(): FeatureForum = FeatureForum(
    this.id,
    this.title,
    this.description,
    this.isHasTopics,
    this.isHasForums,
    this.iconUrl,
    this.parentId
)

private fun FeatureForum.map(): DbForum = DbForum(
    null,
    this.id,
    this.title,
    this.description,
    this.isHasTopics,
    this.isHasForums,
    this.iconUrl,
    this.parentId
)
