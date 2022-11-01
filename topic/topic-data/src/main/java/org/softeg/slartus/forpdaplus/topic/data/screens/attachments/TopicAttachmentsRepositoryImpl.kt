package org.softeg.slartus.forpdaplus.topic.data.screens.attachments

import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.models.TopicAttachmentsResponse
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.models.mapToTopicAttachmentOrNull
import ru.softeg.slartus.forum.api.TopicAttachment
import ru.softeg.slartus.forum.api.TopicAttachmentsRepository
import javax.inject.Inject

class TopicAttachmentsRepositoryImpl @Inject constructor(
    private val remoteTopicAttachmentsDataSource: RemoteTopicAttachmentsDataSource,
    private val topicAttachmentsParser: Parser<TopicAttachmentsResponse>
) : TopicAttachmentsRepository {
    override suspend fun fetchTopicAttachments(topicId: String): List<TopicAttachment> =
        remoteTopicAttachmentsDataSource.fetchTopicAttachments(
            topicId, topicAttachmentsParser.id
        )?.mapNotNull { it.mapToTopicAttachmentOrNull() } ?: emptyList()

}