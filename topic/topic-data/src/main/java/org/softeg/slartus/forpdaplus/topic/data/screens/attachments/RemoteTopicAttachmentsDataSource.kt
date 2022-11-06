package org.softeg.slartus.forpdaplus.topic.data.screens.attachments

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.models.TopicAttachmentResponse
import org.softeg.slartus.hosthelper.HostHelper.Companion.host
import javax.inject.Inject

class RemoteTopicAttachmentsDataSource @Inject constructor(
    private val httpClient: AppHttpClient,
    private val parseFactory: ParseFactory,
    private val topicAttachmentsParser: TopicAttachmentsParser
) {
    suspend fun fetchTopicAttachments(topicId: String): List<TopicAttachmentResponse> = withContext(Dispatchers.IO) {
        val url = "https://${host}/forum/index.php?act=attach&code=showtopic&tid=$topicId"
        val response = httpClient.performGet(url)
        parseFactory.parseAsync(url = url, body = response)
        topicAttachmentsParser.parse(response)
    }
}