package org.softeg.slartus.forpdaplus.feature_news.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.feature_news.data.NewsListRepository
import org.softeg.slartus.forpdaplus.feature_news.ui.UiNewsListItem.Companion.map
import javax.inject.Inject

class NewsListViewModel @Inject constructor(
    private val repository: NewsListRepository
) : ViewModel() {

    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        _uiState.value = UiState.Error(ex)
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Initialize)
    val uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch(errorHandler) {
            repository
                .getNewsList()
                .catch { _uiState.value = UiState.Error(it) }
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _uiState.value = UiState.Success(pagingData.map { it.map() })
                }
        }
    }
}

sealed class UiState {
    object Initialize : UiState()

    data class Success(val items: PagingData<UiNewsListItem>) : UiState()
    data class Error(val exception: Throwable) : UiState()
}