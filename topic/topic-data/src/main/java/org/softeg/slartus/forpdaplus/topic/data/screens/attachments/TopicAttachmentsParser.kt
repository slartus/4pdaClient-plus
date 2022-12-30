package org.softeg.slartus.forpdaplus.topic.data.screens.attachments

import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jsoup.Jsoup
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.models.TopicAttachmentResponse
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.models.TopicAttachmentsResponse
import java.util.regex.Pattern
import javax.inject.Inject

class TopicAttachmentsParser @Inject constructor() : Parser<TopicAttachmentsResponse> {
    override val id: String
        get() = TopicAttachmentsParser::class.java.simpleName

    private val _data = MutableStateFlow(TopicAttachmentsResponse(emptyList()))
    override val data: StateFlow<TopicAttachmentsResponse>
        get() = _data.asStateFlow()

    override fun isOwn(url: String, args: Bundle?): Boolean = urlActPattern.matcher(url).find()

    override suspend fun parse(page: String, args: Bundle?): TopicAttachmentsResponse? {
        val doc = Jsoup.parse(page)
        val attachmentElements = doc.select("tr[id]")
        return TopicAttachmentsResponse(attachmentElements.map { attachmentElement ->
            val id: String? = attachmentElement.attr("id")
            val fileIconUrl: String? =
                attachmentElement.selectFirst("td:eq(0)>img[alt=\"Прикрепленный файл\"]")
                    ?.attr("src")

            val td1Element = attachmentElement.selectFirst("td:eq(1)")
            val urlElement = td1Element?.selectFirst("a")
            val url: String? = urlElement?.attr("href")
            val name: String? = urlElement?.text()

            var date: String? = null
            var count: String? = null
            td1Element?.selectFirst("div.desc")?.text()?.let { description ->
                val matcher = descriptionParser.matcher(description)
                if (matcher.find()) {
                    count = matcher.group(1)?.trim()
                    date = matcher.group(2)?.trim()
                }
            }
            val fileSize: String? = attachmentElement.selectFirst("td:eq(2)")?.text()
            val postUrl: String? = attachmentElement.selectFirst("td:eq(3)>a")?.attr("href")

            TopicAttachmentResponse(
                id = id,
                iconUrl = fileIconUrl,
                url = url,
                name = name,
                date = date,
                size = fileSize,
                postUrl = postUrl,
                count = count
            )
        })

    }

    companion object {
        private val descriptionParser by lazy {
            Pattern.compile(
                """(?:\(\s*Кол-во скачиваний:\s*(\d+)\s*\)\s*)?\(\s*Добавлено\s*([^)]*)\)""",
                Pattern.CASE_INSENSITIVE
            )
        }
        private val urlActPattern by lazy {
            Pattern.compile("""\Wact=attach\W""", Pattern.CASE_INSENSITIVE)
        }
    }
}