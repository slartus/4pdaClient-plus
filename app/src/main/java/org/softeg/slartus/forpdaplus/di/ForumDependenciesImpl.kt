package org.softeg.slartus.forpdaplus.di

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.softeg.slartus.forpdacommon.URIUtils
import org.softeg.slartus.forpdaplus.core_db.forum.ForumDao
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumDb
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumDependencies
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumPreferences
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumService
import org.softeg.slartus.forpdaplus.prefs.Preferences
import org.softeg.slartus.hosthelper.HostHelper
import ru.slartus.http.Http
import javax.inject.Inject
import org.softeg.slartus.forpdaplus.core_db.forum.Forum as DbForum
import org.softeg.slartus.forpdaplus.core.repositories.Forum as FeatureForum

class ForumDependenciesImpl @Inject constructor(
    override val forumsService: ForumService,
    override val forumsDb: ForumDb,
    override val forumPreferences: ForumPreferences
) : ForumDependencies

class ForumServiceImpl @Inject constructor() : ForumService {
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

    override fun markAsRead(forumId: String) {
        val queryParams =
            mapOf("act" to "login", "CODE" to "04", "f" to forumId, "fromforum" to forumId)

        val uri =
            URIUtils.createURI("http", HostHelper.host, "/forum/index.php", queryParams, "UTF-8")


        Http.instance.performGet(uri)
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

class ForumPreferencesImpl @Inject constructor(
) : ForumPreferences {
    override fun setStartForum(id: String?, title: String?) {
        Preferences.List.setStartForum(id, title)
    }

    override val showImages: Boolean
        get() = Preferences.Forums.isShowImages
    override val startForumId: String?
        get() = Preferences.List.startForumId

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
