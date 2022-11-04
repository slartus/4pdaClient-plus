package ru.softeg.slartus.forum.api

interface TopicAttachmentsRepository {
    suspend fun fetchTopicAttachments(topicId: String): List<TopicAttachment>
}