package org.softeg.slartus.forpdaplus.topic.data.screens.attachments

import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.models.mapToTopicAttachmentOrNull
import ru.softeg.slartus.forum.api.TopicAttachment
import ru.softeg.slartus.forum.api.TopicAttachmentsRepository
import javax.inject.Inject

class TopicAttachmentsRepositoryImpl @Inject constructor(
    private val remoteTopicAttachmentsDataSource: RemoteTopicAttachmentsDataSource
) : TopicAttachmentsRepository {
    override suspend fun fetchTopicAttachments(topicId: String): List<TopicAttachment> =
        remoteTopicAttachmentsDataSource.fetchTopicAttachments(
            topicId
        ).mapNotNull { it.mapToTopicAttachmentOrNull() }
}