package org.softeg.slartus.forpdaplus.topic.data.screens.users.parsers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.softeg.slartus.forpdacommon.getQueryParameterOrNull
import org.softeg.slartus.forpdacommon.toUriOrNull
import ru.softeg.slartus.forum.api.TopicWriter
import javax.inject.Inject

class TopicWritersParser @Inject constructor() {
    suspend fun parse(page: String): List<TopicWriterResponse> = withContext(Dispatchers.Default) {
        val document = Jsoup.parse(page)

        document.select("div.post_header")?.mapNotNull { divElement ->
            val userA = divElement.selectFirst("a[href*=showuser]") ?: return@mapNotNull null
            val id = userA.attr("href").toUriOrNull()?.getQueryParameterOrNull("showuser")

            val nick = userA.text()
            val messagesCount = divElement.ownText()?.toIntOrNull() ?: 0
            return@mapNotNull TopicWriterResponse(id, nick, messagesCount)
        } ?: emptyList()
    }
}

data class TopicWriterResponse(val id: String?, val nick: String, val postsCount: Int?)

fun TopicWriterResponse.toTopicWriterOrNull(): TopicWriter? {
    return TopicWriter(
        id = id ?: return null,
        nick = nick,
        postsCount = postsCount ?: 0
    )
}