package org.softeg.slartus.forpdaplus.topic.data.screens.users.parsers

import org.jsoup.Jsoup
import org.softeg.slartus.forpdacommon.getQueryParameterOrNull
import org.softeg.slartus.forpdacommon.toUriOrNull
import ru.softeg.slartus.forum.api.TopicWriter
import javax.inject.Inject

class TopicWritersParser @Inject constructor() {
    fun parse(page: String): List<TopicWriterResponse> {
        val document = Jsoup.parse(page)

        return document.selectFirst("table:has(tr>th:contains(Автор))")?.select("tr:has(td)")
            ?.mapNotNull { trElement ->
                val element = trElement.selectFirst("td:eq(0)>a") ?: return@mapNotNull null
                val id = element.attr("href").toUriOrNull()?.getQueryParameterOrNull("showuser")
                val nick = element.text()
                val messagesCount =
                    trElement.selectFirst("td:eq(1)")?.text()?.toIntOrNull() ?: 0
                TopicWriterResponse(id, nick, messagesCount)
            } ?: emptyList()
    }
}

data class TopicWriterResponse(val id: String?, val nick: String?, val postsCount: Int?)

fun TopicWriterResponse.toTopicWriterOrNull(): TopicWriter? {
    return TopicWriter(
        id = id ?: return null,
        nick = nick ?: return null,
        postsCount = postsCount ?: 0
    )
}