package org.softeg.slartus.forpdaplus.topic.data.screens.attachments.models

import ru.softeg.slartus.forum.api.TopicAttachment

data class TopicAttachmentResponse(
    val id: String? = null,
    val iconUrl: String? = null,
    val url: String? = null,
    val name: String? = null,
    val date: String? = null,
    val size: String? = null,
    val postUrl: String? = null
)


class TopicAttachmentsResponse(list: List<TopicAttachmentResponse>) : List<TopicAttachmentResponse> by list

fun TopicAttachmentResponse.mapToTopicAttachmentOrNull(): TopicAttachment? {
    return TopicAttachment(
        id = id ?: return null,
        url = url ?: return null,
        iconUrl = iconUrl.orEmpty(),
        name = name ?: "not parsed",
        date = date.orEmpty(),
        size = size.orEmpty(),
        postUrl = postUrl.orEmpty()
    )
}