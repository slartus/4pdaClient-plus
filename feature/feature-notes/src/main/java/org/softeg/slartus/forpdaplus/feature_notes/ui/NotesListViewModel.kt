package org.softeg.slartus.forpdaplus.feature_notes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.feature_notes.data.NotesRepository
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(private val repository: NotesRepository) :
    ViewModel() {
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

            repository.notes
                .distinctUntilChanged()
                .collect { items ->
                    _uiState.value = NotesListState.Success(items.map { NoteListItem(it, false) })
                }

        }
    }
}

sealed class NotesListState {
    object Initialize : NotesListState()
    data class Success(val items: List<NoteListItem>) : NotesListState()
    data class Error(val exception: Throwable) : NotesListState()
}