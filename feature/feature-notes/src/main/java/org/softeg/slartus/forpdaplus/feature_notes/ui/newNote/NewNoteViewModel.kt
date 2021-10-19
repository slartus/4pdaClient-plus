package org.softeg.slartus.forpdaplus.feature_notes.ui.newNote

import android.os.Parcelable
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import org.softeg.slartus.forpdaplus.core_ui.di.ViewModelAssistedFactory
import org.softeg.slartus.forpdaplus.core_ui.ui.model.SingleLiveEvent
import org.softeg.slartus.forpdaplus.feature_notes.data.NotesRepository
import javax.inject.Inject

class NewNoteViewModel constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: NotesRepository
) :
    ViewModel() {
    private val _state: MutableLiveData<NewNoteViewState> =
        if (savedStateHandle.contains(STATE_KEY)) savedStateHandle.getLiveData(STATE_KEY)
        else
            MutableLiveData(
                NewNoteViewState(
                    title = savedStateHandle.get(TITLE_KEY),
                    body = savedStateHandle.get(BODY_KEY),
                    url = savedStateHandle.get(URL_KEY),
                    topicId = savedStateHandle.get(TOPIC_ID_KEY),
                    topicTitle = savedStateHandle.get(TOPIC_TITLE_KEY),
                    postId = savedStateHandle.get(POST_ID_KEY),
                    userId = savedStateHandle.get(USER_ID_KEY),
                    userName = savedStateHandle.get(USER_NAME_KEY)
                )
            )
    val error = SingleLiveEvent<Throwable>()

    val closeEvent = SingleLiveEvent<Unit>()

    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        error.value = ex
    }

    val state: LiveData<NewNoteViewState> = _state

    fun saveState() {
        savedStateHandle.set(STATE_KEY, _state.value)
    }

    fun saveNote() {
        _state.value?.let { data ->
            viewModelScope.launch(Dispatchers.Default + errorHandler) {
                repository.createNote(
                    title = data.title,
                    body = data.body,
                    url = data.url,
                    topicId = data.topicId,
                    topic = data.topicTitle,
                    postId = data.postId,
                    userId = data.userId,
                    user = data.userName
                )
                withContext(Dispatchers.Main) {
                    closeEvent.call()
                }
            }
        }
    }

    fun setTitle(title: String) {
        _state.value?.title = title
    }

    fun setBody(body: String) {
        _state.value?.body = body
    }

    fun clearTitle() {
        _state.value = _state.value?.copy(title = "")
    }

    fun clearBody() {
        _state.value = _state.value?.copy(body = "")
    }

    companion object {
        private const val STATE_KEY = "state"
        const val TITLE_KEY = "title"
        const val BODY_KEY = "body"
        const val URL_KEY = "url"
        const val TOPIC_ID_KEY = "topicId"
        const val TOPIC_TITLE_KEY = "topicTitle"
        const val POST_ID_KEY = "postId"
        const val USER_ID_KEY = "userId"
        const val USER_NAME_KEY = "userName"
    }
}

@Parcelize
data class NewNoteViewState(
    var title: String? = null,
    var body: String? = null,
    val url: String? = null,
    val topicId: String? = null,
    val topicTitle: String? = null,
    val postId: String? = null,
    val userId: String? = null,
    val userName: String? = null
) : Parcelable

class NewNoteViewModelFactory @Inject constructor(
    private val repository: NotesRepository
) : ViewModelAssistedFactory<NewNoteViewModel> {
    override fun create(handle: SavedStateHandle) =
        NewNoteViewModel(handle, repository)
}
