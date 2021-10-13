package org.softeg.slartus.forpdaplus.feature_notes.ui.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdaplus.core.di.ViewModelAssistedFactory
import org.softeg.slartus.forpdaplus.feature_notes.Note
import org.softeg.slartus.forpdaplus.feature_notes.data.NotesRepository
import javax.inject.Inject

class NoteViewModel constructor(
    state: SavedStateHandle,
    private val repository: NotesRepository
) :
    ViewModel() {

    private val noteId: Int = state.get<Int>(NoteFragment.ARG_NOTE_ID)!!

    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        _uiState.value = NoteUIState.Error(ex)
    }

    private val _uiState = MutableStateFlow<NoteUIState>(NoteUIState.Initialize)
    val uiState: StateFlow<NoteUIState> = _uiState

    init {
        viewModelScope.launch(errorHandler) {
            repository.load()

            _uiState.value = repository.getNote(noteId)?.let {
                NoteUIState.Success(it)
            }?: NoteUIState.Error(NotReportException("note $noteId not found"))
        }
    }
}

class NoteViewModelFactory @Inject constructor(
    private val repository: NotesRepository
) : ViewModelAssistedFactory<NoteViewModel> {
    override fun create(handle: SavedStateHandle) =
        NoteViewModel(handle, repository)
}

sealed class NoteUIState {
    object Initialize : NoteUIState()
    data class Success(val item: Note) : NoteUIState()
    data class Error(val exception: Throwable) : NoteUIState()
}