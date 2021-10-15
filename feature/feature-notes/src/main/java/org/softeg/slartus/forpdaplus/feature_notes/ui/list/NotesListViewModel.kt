package org.softeg.slartus.forpdaplus.feature_notes.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.di.ViewModelAssistedFactory
import org.softeg.slartus.forpdaplus.feature_notes.data.NotesRepository
import javax.inject.Inject

class NotesListViewModel constructor(
    state: SavedStateHandle,
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
    }

    private val _uiState = MutableStateFlow<NotesListState>(NotesListState.Initialize)
    val uiState: StateFlow<NotesListState> = _uiState

    init {
        viewModelScope.launch(errorHandler) {
            repository.load()

            launch {
                repository.notes
                    .distinctUntilChanged()
                    .collect { items ->
                        _uiState.value = NotesListState.Success(
                            items
                                .filter { topicId == null || it.topicId == topicId }
                                .map { NoteListItem(it, false) }
                        )
                    }
            }
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