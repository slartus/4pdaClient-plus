package org.softeg.slartus.forpdaplus.topic.impl.screens.attachments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core_lib.viewmodel.BaseViewModel
import ru.softeg.slartus.forum.api.TopicAttachmentsRepository
import javax.inject.Inject


@HiltViewModel
class TopicAttachmentsViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val topicAttachmentsRepository: TopicAttachmentsRepository
) : BaseViewModel<TopicAttachmentsViewState, TopicAttachmentsAction, TopicAttachmentsEvent>(
    initialState = TopicAttachmentsViewState()
) {
    init {
        fetchData()
    }

    private var topicId: String? = state[ARG_TOPIC_ID]
        set(value) {
            field = value
            state[ARG_TOPIC_ID] = value
        }

    fun setArguments(topicId: String) {
        this.topicId = this.topicId ?: topicId
    }

    override fun obtainEvent(viewEvent: TopicAttachmentsEvent) = when (viewEvent) {
        TopicAttachmentsEvent.ActionInvoked -> viewAction = null
        is TopicAttachmentsEvent.OnHiddenChanged -> fetchData()
        TopicAttachmentsEvent.ReloadClicked -> fetchData()
        TopicAttachmentsEvent.OnReverseOrderClicked -> handleOnReverseOrderClicked()
    }

    private fun handleOnReverseOrderClicked() {
        val attachments = viewState.attachments
        viewState = viewState.copy(attachments = emptyList(), loading = true)
        viewModelScope.launch(Dispatchers.Default) {
            delay(REVERS_DELAY)
            viewState = viewState.copy(attachments = attachments.asReversed(), loading = false)
        }
    }

    private fun fetchData() {
        val topicId = topicId ?: return

        viewState = viewState.copy(loading = true)
        viewModelScope.launch {
            kotlin.runCatching {
                val items = topicAttachmentsRepository.fetchTopicAttachments(topicId).map {
                    it.mapToTopicAttachmentModel()
                }
                viewState = viewState.copy(loading = false, attachments = items)
            }.onFailure {
                viewState = viewState.copy(loading = false)
            }
        }
    }

    companion object {
        private const val REVERS_DELAY = 300L
        private const val ARG_TOPIC_ID = "TopicAttachmentsFragment.ARG_TOPIC_ID"
    }
}