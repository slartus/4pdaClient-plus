package org.softeg.slartus.forpdaplus.feature_notes.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core_ui.di.ViewModelAssistedFactory
import org.softeg.slartus.forpdaplus.feature_notes.data.NotesRepository
import javax.inject.Inject

class NotesListViewModel constructor(
    private val state: SavedStateHandle,
    private val repository: NotesRepository
) :
    ViewModel() {

    val topicId = state.get<String>(NotesListFragment.ARG_TOPIC_ID)

    fun delete(id: String) {
        viewModelScope.launch(errorHandler) {
            repository.delete(id)
        }
    }

    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        _uiState.value = NotesListState.Error(ex)
        _loading.value = false
    }

    private val _uiState = MutableStateFlow<NotesListState>(NotesListState.Initialize)
    val uiState: StateFlow<NotesListState> = _uiState

    private val _reloadFlag = MutableStateFlow(true)
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        _loading.value = true
        viewModelScope.launch(errorHandler) {
            repository.load()

            repository.notes
                .distinctUntilChanged()
                .collect { items ->
                    _uiState.value = NotesListState.Success(
                        items
                            .filter { topicId == null || it.topicId == topicId }
                            .sortedByDescending { it.date }
                            .map { NoteListItem(it, false) }
                    )
                    _loading.value = false
                }
        }
    }

    fun saveState() {
        state.set(NotesListFragment.ARG_TOPIC_ID, topicId)
    }

    fun reload() {
        _reloadFlag.value = !_reloadFlag.value
        _loading.value = true
        viewModelScope.launch(errorHandler) {
            repository.load()
            _loading.value = false
        }
    }
}

class NotesListViewModelFactory @Inject constructor(
    private val repository: NotesRepository
) : ViewModelAssistedFactory<NotesListViewModel> {
    override fun create(handle: SavedStateHandle) =
        NotesListViewModel(handle, repository)

}

sealed class NotesListState {
    object Initialize : NotesListState()

    data class Success(val items: List<NoteListItem>) : NotesListState()
    data class Error(val exception: Throwable) : NotesListState()
}