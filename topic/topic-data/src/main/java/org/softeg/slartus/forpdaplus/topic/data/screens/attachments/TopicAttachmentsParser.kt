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

    override fun isOwn(url: String, args: Bundle?): Boolean = UrlActPattern.matcher(url).find()

    override suspend fun parse(page: String, args: Bundle?): TopicAttachmentsResponse? {
        val doc = Jsoup.parse(page)
        val attachmentElements = doc.select("tr[id]")
        val dateTrimChars =  setOf('(',')',' ')
        return TopicAttachmentsResponse(attachmentElements.map { attachmentElement ->
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

//        val body: String =
//            httpClient.performGet("https://" + host + "/forum/index.php?act=attach&code=showtopic&tid=" + topicId).responseBody
//
//        val m = Pattern.compile(
//            "<tr id=\"(\\d+)\">
        //            <td align=\"center\" class=\"row1\"><img src=\"[^\"]*/([^.]*)\\..*?\" alt=\"Прикрепленный файл\" /></td><td class=\"row2\">
        //            <a href=\"([^\"]*)\" target=\"_blank\">([^<]*)</a><div class=\"desc\">\\(([^)]*)\\)</div></td><td align=\"center\" class=\"row1\">([^<]*)</td><td class=\"row2\" align=\"center\"><a href=\"#\" onclick=\"opener.location='[^']*pid=(\\d+)';\">\\d+</a></td></tr>",
//            Pattern.CASE_INSENSITIVE
//        ).matcher(body)
//        val res: ArrayList<PostAttach> = ArrayList<PostAttach>()
//        val today = Functions.getToday()
//        val yesterday = Functions.getYesterToday()
//        while (m.find()) {
//            val item = PostAttach()
//            item.setId(m.group(1))
//            item.setFileType(m.group(2))
//            item.setUrl("https://" + host + m.group(3))
//            item.setName(m.group(4))
//            item.setAdditionDate(
//                Functions.parseForumDateTime(
//                    m.group(5).replace("Добавлено ", ""),
//                    today,
//                    yesterday
//                )
//            )
//            item.setFileSize(FileUtils.parseFileSize(m.group(6)))
//            item.setPostId(m.group(7))
//            res.add(item) // обратная сортировка
//        }
//
//        return res
    }

    companion object {
        private val UrlActPattern by lazy {
            Pattern.compile("""\Wact=attach\W""", Pattern.CASE_INSENSITIVE)
        }
    }
}