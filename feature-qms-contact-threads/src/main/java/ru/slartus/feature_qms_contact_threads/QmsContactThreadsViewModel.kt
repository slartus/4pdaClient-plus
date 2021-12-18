package ru.slartus.feature_qms_contact_threads

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
import org.softeg.slartus.forpdaplus.core.repositories.QmsThreadsRepository
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import ru.slartus.feature_qms_contact_threads.fingerprints.QmsThreadItem
import javax.inject.Inject

@HiltViewModel
class QmsContactThreadsViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val qmsThreadsRepository: QmsThreadsRepository

) : ViewModel() {
    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        _events.value = Event.Error(ex)
    }

    private val _events = MutableStateFlow<Event>(Event.Empty)
    val events: StateFlow<Event> = _events.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Initialize)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private var contactId: String? = state[QmsContactThreadsFragment.ARG_CONTACT_ID]
        set(value) {
            field = value
            state[QmsContactThreadsFragment.ARG_CONTACT_ID] = value
        }

    init {
        reload()
        viewModelScope.launch {
            launch(errorHandler) {
                qmsThreadsRepository.threads
                    .distinctUntilChanged()
                    .collect { rawItems ->
                        val items = rawItems.map {
                            QmsThreadItem(
                                id = it.id ?: "",
                                title = it.title ?: "",
                                messagesCount = it.messagesCount ?: 0,
                                newMessagesCount = it.newMessagesCount ?: 0
                            )
                        }
                        _uiState.emit(UiState.Items(items))
                    }
            }
        }
    }

    fun setArguments(arguments: Bundle?) {
        contactId =
            this.contactId ?: arguments?.getString(QmsContactThreadsFragment.ARG_CONTACT_ID, null)
                    ?: error("contactId not initialized")
    }

    private fun reload() {
        val contactId = contactId ?: return
        viewModelScope.launch(errorHandler) {
            _loading.emit(true)
            try {
                qmsThreadsRepository.load(contactId)
            } finally {
                _loading.emit(false)
            }
        }
    }

    fun onReloadClick() {
        reload()
    }

    fun onEventReceived() {
        viewModelScope.launch(errorHandler) {
            _events.emit(Event.Empty)
        }
    }

    sealed class UiState {
        object Initialize : UiState()
        data class Items(val items: List<Item>) : UiState()
    }

    sealed class Event {
        object Empty : Event()
        data class Error(val exception: Throwable) : Event()
        data class ShowToast(
            @StringRes val resId: Int,// maybe need use enum for clear model
            val duration: Int = Toast.LENGTH_SHORT
        ) : Event()
    }
}