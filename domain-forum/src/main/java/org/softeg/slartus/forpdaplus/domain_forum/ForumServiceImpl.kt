package org.softeg.slartus.forpdaplus.domain_forum

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.softeg.slartus.forpdacommon.URIUtils
import org.softeg.slartus.forpdaplus.core.entities.Forum
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.forpdaplus.core.services.ForumService
import org.softeg.slartus.hosthelper.HostHelper
import javax.inject.Inject

class ForumServiceImpl @Inject constructor(private val httpClient: AppHttpClient) : ForumService {

    override suspend fun getGithubForum(): List<Forum> {
        val response = httpClient
            .performGet("https://raw.githubusercontent.com/slartus/4pdaClient-plus/master/forum_struct.json")

        val itemsListType = object : TypeToken<List<ForumImpl>>() {}.type
        return Gson().fromJson(response, itemsListType)
    }

    override suspend fun getSlartusForum(): List<Forum> {
        val response = httpClient
            .performGet("http://slartus.ru/4pda/forum_struct.json")

        val itemsListType = object : TypeToken<List<ForumImpl>>() {}.type
        return Gson().fromJson(response, itemsListType)
    }

    override suspend fun markAsRead(forumId: String) {
        val queryParams =
            mapOf("act" to "login", "CODE" to "04", "f" to forumId, "fromforum" to forumId)

        val uri =
            URIUtils.createURI("http", HostHelper.host, "/forum/index.php", queryParams, "UTF-8")

        httpClient.performGet(uri)
    }

    override fun getForumUrl(forumId: String?): String {
        val baseUrl = "https://${HostHelper.host}/forum/index.php"
        return if (forumId == null)
            baseUrl
        else
            "$baseUrl?showforum=$forumId"
    }
}