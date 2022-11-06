package org.softeg.slartus.forpdaplus.forum.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdacommon.NameValuePair
import org.softeg.slartus.forpdacommon.URIUtils
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import ru.softeg.slartus.forum.api.ForumService
import org.softeg.slartus.forpdaplus.forum.data.models.ForumItemResponse
import org.softeg.slartus.forpdaplus.forum.data.models.mapToForumItemOrNull
import org.softeg.slartus.hosthelper.HostHelper
import ru.softeg.slartus.forum.api.ForumItem

import javax.inject.Inject

class ForumServiceImpl @Inject constructor(private val httpClient: AppHttpClient) : ForumService {

    override suspend fun getGithubForum(): List<ForumItem> = withContext(Dispatchers.IO) {
        val response = httpClient
            .performGet("https://raw.githubusercontent.com/slartus/4pdaClient-plus/master/forum_struct.json")

        val itemsListType = object : TypeToken<List<ForumItemResponse>>() {}.type
        val responseItems: List<ForumItemResponse> = Gson().fromJson(response, itemsListType)
        responseItems.mapNotNull { it.mapToForumItemOrNull() }
    }

    override suspend fun getSlartusForum(): List<ForumItem> = withContext(Dispatchers.IO) {
        val response = httpClient
            .performGet("http://slartus.ru/4pda/forum_struct.json")

        val itemsListType = object : TypeToken<List<ForumItemResponse>>() {}.type
        val responseItems: List<ForumItemResponse> = Gson().fromJson(response, itemsListType)
        responseItems.mapNotNull { it.mapToForumItemOrNull() }
    }

    override suspend fun markAsRead(forumId: String) = withContext(Dispatchers.IO) {
        val queryParams =
            mapOf("act" to "login", "CODE" to "04", "f" to forumId, "fromforum" to forumId)
                .map {
                    NameValuePair(it.key, it.value)
                }

        val uri =
            URIUtils.createURI("http", HostHelper.host, "/forum/index.php", queryParams, "UTF-8")

        httpClient.performGet(uri)
        Unit
    }

    override fun getForumUrl(forumId: String?): String {
        val baseUrl = "https://${HostHelper.host}/forum/index.php"
        return if (forumId == null)
            baseUrl
        else
            "$baseUrl?showforum=$forumId"
    }
}