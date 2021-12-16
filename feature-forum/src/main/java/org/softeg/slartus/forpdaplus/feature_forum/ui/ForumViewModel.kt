package org.softeg.slartus.forpdaplus.feature_forum.ui

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.AppActions
import org.softeg.slartus.forpdaplus.core.ForumPreferences
import org.softeg.slartus.forpdaplus.core.entities.Forum
import org.softeg.slartus.forpdaplus.core.entities.SearchSettings
import org.softeg.slartus.forpdaplus.core.repositories.ForumRepository
import org.softeg.slartus.forpdaplus.core.repositories.UserInfoRepository
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.feature_forum.R
import org.softeg.slartus.forpdaplus.feature_forum.entities.ForumItem
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.CrumbItem
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumDataItem
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.TopicsItemItem
import javax.inject.Inject

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val userInfoRepository: UserInfoRepository,
    private val forumRepository: ForumRepository,
    private val forumPreferences: ForumPreferences,
    private val appActions: AppActions
) : ViewModel() {
    val showImages: Boolean = forumPreferences.showImages
    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        _events.value = Event.Error(ex)
    }

    private val _events = MutableStateFlow<Event>(Event.Empty)
    val events: StateFlow<Event> = _events.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Initialize)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private var items = emptyList<Forum>()
    private var crumbs = emptyList<Forum>()
    private var userLogined = false

    var forumId: String? = state.get(ForumFragment.FORUM_ID_KEY)
        set(value) {
            field = value
            state[ForumFragment.FORUM_ID_KEY] = value
        }

    init {
        reload()
        viewModelScope.launch {
            launch(errorHandler) {
                forumRepository.forum
                    .distinctUntilChanged()
                    .collect { rawItems ->
                        items = rawItems.map {
                            ForumItem(
                                id = it.id,
                                title = it.title,
                                description = it.description,
                                isHasTopics = it.isHasTopics,
                                isHasForums = it.isHasForums,
                                iconUrl = it.iconUrl,
                                parentId = it.parentId
                            )
                        }

                        refreshDataState(false)
                    }
            }
            launch(errorHandler) {
                userInfoRepository.userInfo
                    .distinctUntilChanged()
                    .collect {
                        userLogined = it.logined
                    }
            }
        }
    }

    fun setArguments(arguments: Bundle?) {
        forumId = this.forumId ?: arguments?.getString(ForumFragment.FORUM_ID_KEY, null)
                ?: forumPreferences.startForumId
        refreshDataState(false)
    }

    fun reload() {
        viewModelScope.launch(errorHandler) {
            _loading.value = true
            try {
                forumRepository.load()
            } finally {
                _loading.value = false
            }
        }
    }

    fun onMarkAsReadClick() {
        if (!userLogined) {
            _events.value = Event.ShowToast(R.string.need_login)
        } else {
            _events.value = Event.MarkAsReadConfirmDialog
        }
    }

    fun onMarkAsReadConfirmClick() {
        _events.value = Event.ShowToast(R.string.request_sent)
        getCurrentForum()?.let { f ->
            markForumRead(f.id ?: "-1")
        }
    }

    fun onSetForumStartingClick() {
        getCurrentForum()?.let { f ->
            setStartForum(f.id)
        }
    }

    fun onCrumbClick(forumId: String?) {
        val forum = items.firstOrNull { it.id == forumId }
        if (forum?.isHasForums == true || forumId == null)
            refreshDataState(forumId)
        else if (forum != null)
            appActions.showForumTopicsList(forum.id, forum.title)
    }

    fun onCrumbLongClick(forumId: String?) {
        _events.value = Event.ShowUrlMenu(forumRepository.getForumUrl(forumId))
    }

    fun onForumClick(forumId: String?) {
        val forum = items.firstOrNull { it.id == forumId } ?: return
        if (forum.isHasForums) {
            refreshDataState(forum.id)
        } else {
            appActions.showForumTopicsList(forum.id, forum.title)
        }
    }

    fun onForumLongClick(forumId: String?) {
        _events.value = Event.ShowUrlMenu(forumRepository.getForumUrl(forumId))
    }

    private fun refreshDataState(forumId: String?) {
        this.forumId = forumId
        refreshDataState(true)
    }

    private fun refreshDataState(scrollToTop: Boolean) {
        crumbs = buildCrumbs(items)

        val currentItems = items.filter { it.parentId == forumId }
            .map {
                ForumDataItem(
                    it.id,
                    it.title,
                    it.description,
                    it.iconUrl,
                    it.isHasForums
                )
            }
        val currentForum = getCurrentForum()
        val topicsList =
            if (currentForum?.isHasTopics == true) listOf(TopicsItemItem(currentForum.id))
            else emptyList()
        val items = currentItems + topicsList

        val crumbItems = crumbs.map { CrumbItem(it.id, it.title) }
        _uiState.value = UiState.Items(crumbItems, items, scrollToTop)
    }

    private fun buildCrumbs(items: List<Forum>): List<Forum> {
        if (items.isEmpty()) return emptyList()
        val crumbs = ArrayList<Forum>()
        var f = forumId
        while (true) {
            if (f == null) {
                crumbs.add(0, ForumItem(null, "4PDA"))
                break
            } else {
                val parent = items.firstOrNull { it.id == f }
                f = if (parent == null) {
                    crumbs.add(0, ForumItem(f, parent?.title ?: "Not Found"))
                    null
                } else {
                    crumbs.add(0, parent)
                    parent.parentId
                }
            }
        }
        return crumbs
    }

    fun onBack(): Boolean {
        if (crumbs.size > 1) {
            refreshDataState(crumbs.dropLast(1).last().id)
            return true
        }
        return false
    }

    private fun getCurrentForum(): Forum? = items.firstOrNull { it.id == forumId }

    private fun markForumRead(forumId: String) {
        viewModelScope.launch(errorHandler) {
            forumRepository.markAsRead(forumId)
            _events.value = Event.ShowToast(R.string.forum_setted_read)
        }
    }

    fun getSearchSettings(): SearchSettings {
        val forumIds = forumId?.let { setOf(it) } ?: emptySet()
        return SearchSettings(
            sourceType = SearchSettings.SourceType.All,
            forumIds = forumIds
        )
    }

    private fun setStartForum(id: String?) {
        forumPreferences.startForumId = id
        _events.value = Event.ShowToast(R.string.forum_setted_to_start)
    }

    fun onEventReceived() {
        _events.value = Event.Empty
    }

    fun onTopicsClick(id: String?) {
        val forum = items.firstOrNull { it.id == id } ?: return
        appActions.showForumTopicsList(forum.id, forum.title)
    }

    sealed class UiState {
        object Initialize : UiState()
        data class Items(val crumbs: List<Item>, val items: List<Item>, val scrollToTop: Boolean) :
            UiState()
    }

    sealed class Event {
        object Empty : Event()
        data class Error(val exception: Throwable) : Event()
        data class ShowToast(
            @StringRes val resId: Int,// maybe need use enum for clear model
            val duration: Int = Toast.LENGTH_SHORT
        ) : Event()

        object MarkAsReadConfirmDialog : Event()
        data class ShowUrlMenu(val url: String) : Event()
    }
}

