package org.softeg.slartus.forpdaplus.listfragments.next.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaplus.feature_forum.repository.ForumRepository
import org.softeg.slartus.forpdaplus.repositories.UserInfo
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepository
import javax.inject.Inject

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val forumRepository: ForumRepository,
    private val userInfoRepository: UserInfoRepository
) : ViewModel() {
    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        _uiState.value = ViewState.Error(ex)
    }

    private val _uiState = MutableStateFlow<ViewState>(ViewState.Initialize)
    val uiState: StateFlow<ViewState> = _uiState

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading
    private var items = emptyList<Forum>()
    var forumId: String? = null
    var userInfo: UserInfo? = null
        private set

    init {
        _loading.value = true

        viewModelScope.launch(Dispatchers.Default + errorHandler) {
            launch {
                forumRepository.forum
                    .distinctUntilChanged()
                    .collect { rawItems ->
                        items = rawItems.map {
                            Forum(
                                it.id, it.title ?: "no title"
                            ).apply {
                                description = it.description
                                isHasTopics = it.isHasTopics
                                isHasForums = it.isHasForums
                                iconUrl = it.iconUrl
                                parentId = it.parentId
                            }
                        }
                        refreshDataState()

                        _loading.value = false
                    }
            }
        }
    }

    private fun refreshDataState() {
        val currentItems = items.filter { it.parentId == forumId }
        val crumbs = buildCrumbs(items)
        _uiState.value = ViewState.Success(currentItems, crumbs)
    }

    private fun buildCrumbs(items: List<Forum>): List<Forum> {
        val crumbs = ArrayList<Forum>()
        var f = forumId
        while (true) {
            if (f == null) {
                crumbs.add(0, Forum(null, "4PDA"))
                break
            } else {
                val parent = items.firstOrNull { it.id == f }
                f = if (parent == null) {
                    crumbs.add(0, Forum(f, parent?.title ?: "Not Found"))
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
        refreshDataState()
    }

    fun load() {
        viewModelScope.launch(Dispatchers.Default + errorHandler) {
            forumRepository.load()
        }
    }

    sealed class ViewState {
        object Initialize : ViewState()

        data class Success(val items: List<Forum>, val crumbs: List<Forum>) : ViewState()
        data class Error(val exception: Throwable) : ViewState()
    }
}