package org.softeg.slartus.forpdaplus.di

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdacommon.URIUtils
import org.softeg.slartus.forpdaplus.core_db.forum.ForumDao
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumDb
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumDependencies
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumService
import org.softeg.slartus.forpdaplus.feature_forum.entities.ForumItem
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment
import org.softeg.slartus.hosthelper.HostHelper
import ru.slartus.http.Http
import javax.inject.Inject
import org.softeg.slartus.forpdaplus.core_db.forum.Forum as DbForum

class ForumDependenciesImpl @Inject constructor(
    override val forumsService: ForumService,
    override val forumsDb: ForumDb
) : ForumDependencies {
    override fun showForumTopicsList(forumId: String?, forumTitle: String?) {
        ForumTopicsListFragment.showForumTopicsList(forumId, forumTitle)
    }
}

class ForumServiceImpl @Inject constructor() : ForumService {
    override suspend fun getGithubForum(): List<ForumItem> {
        return withContext(Dispatchers.IO) {
            val response = Http.instance
                .performGet("https://raw.githubusercontent.com/slartus/4pdaClient-plus/master/forum_struct.json")

            val itemsListType = object : TypeToken<List<ForumItem>>() {}.type
            Gson().fromJson(response.responseBody, itemsListType)
        }
    }

    override suspend fun getSlartusForum(): List<ForumItem> {
        return withContext(Dispatchers.IO) {
            val response = Http.instance
                .performGet("http://slartus.ru/4pda/forum_struct.json")

            val itemsListType = object : TypeToken<List<ForumItem>>() {}.type
            Gson().fromJson(response.responseBody, itemsListType)
        }
    }

    override fun markAsRead(forumId: String) {
        val queryParams =
            mapOf("act" to "login", "CODE" to "04", "f" to forumId, "fromforum" to forumId)

        val uri =
            URIUtils.createURI("http", HostHelper.host, "/forum/index.php", queryParams, "UTF-8")


        Http.instance.performGet(uri)
    }

    override fun getForumUrl(forumId: String?): String {
        val baseUrl = "https://${HostHelper.host}/forum/index.php"
        return if (forumId == null)
            baseUrl
        else
            "$baseUrl?showforum=$forumId"
    }
}

class ForumDbImpl @Inject constructor(
    private val forumDao: ForumDao
) : ForumDb {
    override suspend fun getAll(): List<ForumItem> {
        return withContext(Dispatchers.IO) {
            forumDao.getAll().map { it.map() }
        }
    }

    override suspend fun merge(forums: List<ForumItem>) {
        return withContext(Dispatchers.IO) {
            forumDao.merge(forums.map { it.map() })
        }
    }
}

private fun DbForum.map(): ForumItem = ForumItem(
    this.id,
    this.title,
    this.description,
    this.isHasTopics,
    this.isHasForums,
    this.iconUrl,
    this.parentId
)

private fun ForumItem.map(): DbForum = DbForum(
    null,
    this.id,
    this.title,
    this.description,
    this.isHasTopics,
    this.isHasForums,
    this.iconUrl,
    this.parentId
)
