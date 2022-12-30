package org.softeg.slartus.forpdaplus.topic.impl.screens.attachments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
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
    private var filterJob: Job? = null

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
        is TopicAttachmentsEvent.OnFilterTextChanged -> handleOnFilterTextChanged(viewEvent.text)
    }

    private fun handleOnFilterTextChanged(text: String) {
        viewState = viewState.copy(filter = text)
        filterAttachments()
    }

    private fun filterAttachments() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                delay(FILTER_DELAY)
                val filter = viewState.filter
                if (filter.isEmpty()) {
                    viewState = viewState.copy(filteredItems = viewState.attachments)
                    return@launch
                }
                viewState = viewState.copy(loading = true, filteredItems = emptyList())

                withContext(Dispatchers.Default) {
                    viewState = viewState.copy(
                        filteredItems = viewState.attachments.filter {
                            filter.isEmpty() || it.name.containsWildCards(filter)
                        },
                        loading = false
                    )
                }
            }.onFailure {
                it.printStackTrace()
                viewState = viewState.copy(loading = false)
            }
        }
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
                filterAttachments()
            }.onFailure {
                viewState = viewState.copy(loading = false)
            }
        }
    }

    companion object {
        private const val FILTER_DELAY = 1000L
        private const val REVERS_DELAY = 300L
        private const val ARG_TOPIC_ID = "TopicAttachmentsFragment.ARG_TOPIC_ID"

        /**
         * https://www.customguide.com/word/how-to-use-wildcards-in-word
         */
        fun String.containsWildCards(searchText: String, onError: Boolean = true): Boolean {
            return runCatching {
                val pattern =
                    searchText.replace("([.()^$])".toRegex(), "\\\\$1")
                        // Any single character	h?t will find hat, hot, and h t
                        .replace("*", ".*")
                        // Any number of characters	a*d will find ad, ahead, and as compared
                        .replace("?", ".")
//                        // One or more instances of a character	cor@al will find coral and corral
//                        .replace("@", "+")
                return contains(pattern.toRegex(RegexOption.IGNORE_CASE))
            }.onFailure {
                it.printStackTrace()
            }
                .getOrElse { onError }
        }
    }
}