package org.softeg.slartus.forpdaplus.topic.impl.screens.attachments

import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import ru.softeg.slartus.forum.api.TopicAttachment

data class TopicAttachmentModel(
    val id: String,
    val iconUrl: String,
    val url: String,
    val name: String,
    val date: String,
    val size: String,
    val postUrl: String
) : Item

fun TopicAttachment.mapToTopicAttachmentModel(): TopicAttachmentModel = TopicAttachmentModel(
    id = id,
    iconUrl = iconUrl,
    url = url,
    name = name,
    date = date,
    size = size,
    postUrl = postUrl
)