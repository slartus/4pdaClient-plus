package org.softeg.slartus.forpdaplus.topic.data.screens.users.parsers

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import ru.softeg.slartus.forum.api.TopicReader
import javax.inject.Inject

class TopicReadersParser @Inject constructor() {
    suspend fun parse(page: String): List<TopicReaderResponse> = withContext(Dispatchers.Default) {
        val document = Jsoup.parse(page)

        return@withContext document.select("a[title\$=читает]").map { element ->
            val id = Uri.parse(element.attr("href")).getQueryParameter("showuser").orEmpty()
            TopicReaderResponse(
                id = id,
                nick = element.text(),
                htmlColor = element.selectFirst("span")?.attr("style")?.substringAfterLast(":")
            )
        }
    }
}

data class TopicReaderResponse(val id: String?, val nick: String?, val htmlColor: String?)

fun TopicReaderResponse.toTopicReaderOrNull(): TopicReader? {
    return TopicReader(
        id = id ?: return null,
        nick = nick ?: return null,
        htmlColor = htmlColor.orEmpty()
    )
}