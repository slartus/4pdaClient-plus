package org.softeg.slartus.forpdaplus.topic.impl.screens.attachments

data class TopicAttachmentsViewState(
    val loading: Boolean = false,
    val attachments: List<TopicAttachmentModel> = emptyList()
)

sealed class TopicAttachmentsAction

sealed class TopicAttachmentsEvent {
    object ActionInvoked : TopicAttachmentsEvent()
    data class OnHiddenChanged(val hidden: Boolean) : TopicAttachmentsEvent()
    object ReloadClicked : TopicAttachmentsEvent()
    object OnReverseOrderClicked : TopicAttachmentsEvent()
}