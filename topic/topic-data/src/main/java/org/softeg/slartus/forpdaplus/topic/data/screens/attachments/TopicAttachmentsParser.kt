package org.softeg.slartus.forpdaplus.topic.data.screens.attachments

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.models.TopicAttachmentResponse
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.models.TopicAttachmentsResponse
import javax.inject.Inject

class TopicAttachmentsParser @Inject constructor() {
    suspend fun parse(page: String): TopicAttachmentsResponse = withContext(Dispatchers.Default) {
        val doc = Jsoup.parse(page)
        val attachmentElements = doc.select("tr[id]")
        val dateTrimChars =  setOf('(',')',' ')
        return@withContext TopicAttachmentsResponse(attachmentElements.map { attachmentElement ->
            val id: String? = attachmentElement.attr("id")
            val fileIconUrl: String? =
                attachmentElement.selectFirst("td:eq(0)>img[alt=\"Прикрепленный файл\"]")?.attr("src")

            val td1Element = attachmentElement.selectFirst("td:eq(1)")
            val urlElement = td1Element?.selectFirst("a")
            val url: String? = urlElement?.attr("href")
            val name: String? = urlElement?.text()

            val date: String? = td1Element?.selectFirst("div.desc")?.text()?.replace("Добавлено","")?.trim { it in dateTrimChars}
            val fileSize: String? = attachmentElement.selectFirst("td:eq(2)")?.text()
            val postUrl: String? = attachmentElement.selectFirst("td:eq(3)>a")?.attr("href")

            TopicAttachmentResponse(
                id = id,
                iconUrl = fileIconUrl,
                url = url,
                name = name,
                date = date,
                size = fileSize,
                postUrl = postUrl
            )
        })
    }
}