package org.softeg.slartus.forpdaplus.feature_forum.ui

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.entities.Forum
import org.softeg.slartus.forpdaplus.core.repositories.ForumRepository
import org.softeg.slartus.forpdaplus.core.repositories.UserInfoRepository
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.feature_forum.R
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumPreferences
import org.softeg.slartus.forpdaplus.feature_forum.entities.ForumItem
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumCurrentHeaderItem
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumDataItem
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumHeaderItem
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumNoTopicsHeaderItem
import javax.inject.Inject

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val userInfoRepository: UserInfoRepository,
    private val forumRepository: ForumRepository,
    private val forumPreferences: ForumPreferences,
) : ViewModel() {
    val showImages: Boolean = forumPreferences.showImages
    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        _events.value = Event.Error(ex)
    }

    private val _events = MutableStateFlow<Event>(Event.Initialize)
    val events: StateFlow<Event> = _events

    private val _uiState = MutableStateFlow<UiState>(UiState.Initialize)
    val uiState: StateFlow<UiState> = _uiState

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private var items = emptyList<Forum>()
    private var crumbs = emptyList<Forum>()
    private var _userLogined = false
    val userLogined: Boolean
        get() = _userLogined

    var forumId: String? = state.get(ForumFragment.FORUM_ID_KEY)
        set(value) {
            field = value
            state[ForumFragment.FORUM_ID_KEY] = value
        }

    init {
        _loading.value = true

        viewModelScope.launch(errorHandler) {
            launch {
                forumRepository.load()
            }
            launch {
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

                        _loading.value = false
                    }
            }
            launch {
                userInfoRepository.userInfo.distinctUntilChanged()
                    .collect {
                        _userLogined = it.logined
                    }
            }
        }
    }

    fun setArguments(arguments: Bundle?) {
        forumId = this.forumId ?: arguments?.getString(ForumFragment.FORUM_ID_KEY, null)
                ?: forumPreferences.startForumId
    }

    fun reload() {
        viewModelScope.launch(errorHandler) {
            _loading.value = true
            forumRepository.load()
            _loading.value = false
        }
    }

    private fun refreshDataState(scrollToTop: Boolean) {
        crumbs = buildCrumbs(items)

        val headerItems = crumbs.dropLast(1).map { ForumHeaderItem(it.id, it.title) }
        val currentHeaderItems = listOfNotNull(crumbs.lastOrNull()).map {
            if (it.isHasTopics)
                ForumCurrentHeaderItem(it.id, it.title)
            else
                ForumNoTopicsHeaderItem(it.id, it.title)
        }
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
        val items = headerItems + currentHeaderItems + currentItems
        _uiState.value = UiState.Items(items, scrollToTop)
    }

    private fun buildCrumbs(items: List<Forum>): List<Forum> {
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

    fun refreshDataState(forumId: String?) {
        this.forumId = forumId
        refreshDataState(true)
    }

    fun load() {
        viewModelScope.launch(errorHandler) {
            forumRepository.load()
        }
    }

    fun onBack(): Boolean {
        if (crumbs.size > 1) {
            refreshDataState(crumbs.dropLast(1).last().id)
            return true
        }
        return false
    }

    fun getCurrentForum(): Forum? = items.firstOrNull { it.id == forumId }
    fun markForumRead(forumId: String) {
        viewModelScope.launch(errorHandler) {
            forumRepository.markAsRead(forumId)
            _events.value = Event.ShowToast(R.string.forum_setted_read)
        }
    }

    fun setStartForum(id: String?, title: String?) {
        forumPreferences.setStartForum(id, title)
        _events.value = Event.ShowToast(R.string.forum_setted_to_start)
    }

    sealed class UiState {
        object Initialize : UiState()
        data class Items(val items: List<Item>, val scrollToTop: Boolean) : UiState()
    }

    sealed class Event {
        object Initialize : Event()
        data class Error(val exception: Throwable) : Event()
        data class ShowToast(
            @StringRes val resId: Int,
            val duration: Int = Toast.LENGTH_SHORT
        ) : Event()
    }
}

