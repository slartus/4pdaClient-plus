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
            val aElements = divElement.select("a")
            val userA = aElements.firstOrNull() ?: return@mapNotNull null
            val id = userA.attr("href").toUriOrNull()?.getQueryParameterOrNull("showuser")

            val nick = userA.text()
            val messagesCount = when (aElements.size) {
                MODERATOR_A_COUNT -> aElements[1].text().toIntOrNull()
                else -> divElement.ownText().toIntOrNull()
            }

            return@mapNotNull TopicWriterResponse(id, nick, messagesCount)
        } ?: emptyList()
    }

    companion object {
        private const val MODERATOR_A_COUNT = 2
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