package ru.slartus.feature_qms_new_thread

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.softeg.slartus.forpdaplus.core.entities.UserProfile
import org.softeg.slartus.forpdaplus.core.repositories.QmsThreadsRepository
import org.softeg.slartus.forpdaplus.core.repositories.UserProfileRepository
import org.softeg.slartus.forpdaplus.core_res.R
import javax.inject.Inject

@HiltViewModel
class QmsNewThreadViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val qmsThreadsRepository: QmsThreadsRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    private var contactId: String? = savedStateHandle[QmsNewThreadFragment.ARG_CONTACT_ID]
        set(value) {
            field = value
            savedStateHandle[QmsNewThreadFragment.ARG_CONTACT_ID] = value
        }

    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        _events.value = Event.Error(ex)
    }

    private val _uiState =
        MutableStateFlow(savedStateHandle.get<Data>(KEY_DATA) ?: Data(NewThreadModel(), true))
    val uiState: StateFlow<Data> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<Event>(Event.Empty)
    val events: StateFlow<Event> = _events.asStateFlow()

    private val _contact = MutableStateFlow<UserProfile?>(null)
    val contact: StateFlow<UserProfile?> = _contact.asStateFlow()

    private fun reload() {
        val contactId = contactId
        viewModelScope.launch(errorHandler) {
            _uiState.emit(
                _uiState.value.copy(showUserNick = contactId == null)
            )

            if (contactId != null) {
                _events.emit(Event.Progress(true))
                try {
                    val userProfile = userProfileRepository.getUserProfile(contactId)
                    _contact.emit(userProfile)
                    updateUiState(
                        _uiState.value.copy(
                            model = _uiState.value.model.copy(nick = userProfile?.nick),
                            showUserNick = _uiState.value.showUserNick
                        )
                    )
                } finally {
                    _events.emit(Event.Progress(false))
                }
            }
        }
    }

    private suspend fun updateUiState(data: Data) {
        savedStateHandle.set(KEY_DATA, data)
        _uiState.emit(data)

    }

    fun setArguments(arguments: Bundle?) {
        contactId =
            this.contactId ?: arguments?.getString(QmsNewThreadFragment.ARG_CONTACT_ID, null)
        reload()
    }

    fun onEventReceived() {
        viewModelScope.launch(errorHandler) {
            _events.emit(Event.Empty)
        }
    }

    fun onNickChanged(nick: String?) {
        viewModelScope.launch(errorHandler) {
            updateUiState(
                _uiState.value.copy(
                    model = _uiState.value.model.copy(nick = nick)
                )
            )
        }
    }

    fun onSubjectChanged(subject: String?) {
        viewModelScope.launch(errorHandler) {
            updateUiState(
                _uiState.value.copy(
                    model = _uiState.value.model.copy(subject = subject)
                )
            )
            val uiState = _uiState.value
            val sendEnable =
                listOf(uiState.model.nick, uiState.model.subject, uiState.model.message)
                    .all { !it.isNullOrEmpty() }
            _events.emit(Event.SendEnable(sendEnable))
        }
    }

    fun onMessageChanged(message: String?) {
        viewModelScope.launch(errorHandler) {
            updateUiState(
                _uiState.value.copy(
                    model = _uiState.value.model.copy(message = message)
                )
            )
        }
    }

    fun onSendClick() {
        val contactId = contactId ?: return
        viewModelScope.launch(errorHandler) {
            val data = _uiState.value

            val userNick = data.model.nick ?: return@launch
            val subject = data.model.subject ?: return@launch
            val message = data.model.message ?: return@launch
            _events.emit(Event.Progress(true))
            try {
                val threadId =
                    qmsThreadsRepository.createNewThread(contactId, userNick, subject, message)
                if (threadId.isNullOrEmpty()) {
                    _events.emit(Event.Toast(R.string.error_on_new_thread))
                } else {
                    _events.emit(Event.OpenChat(contactId,userNick, threadId, subject))
                }
            } finally {
                _events.emit(Event.Progress(false))
            }
        }
    }

    companion object {
        private const val KEY_DATA = "QmsNewThreadViewModel.DATA"
    }

    sealed class Event {
        object Empty : Event()
        data class Error(val exception: Throwable) : Event()
        data class Progress(val visible: Boolean) : Event()
        data class SendEnable(val enable: Boolean) : Event()
        data class Toast(@StringRes val resId: Int) : Event()
        data class OpenChat(
            val contactId: String,
            val contactNick: String?,
            val threadId: String,
            val threadTitle: String?,
        ) : Event()
    }

    @Parcelize
    data class Data(val model: NewThreadModel, val showUserNick: Boolean) : Parcelable

    @Parcelize
    data class NewThreadModel(
        val nick: String? = null,
        val subject: String? = null,
        val message: String? = null
    ) : Parcelable
}