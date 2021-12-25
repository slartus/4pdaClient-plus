package ru.slartus.feature_qms_contact_threads

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.softeg.slartus.forpdaplus.core.AppPreferences
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.repositories.QmsContactsRepository
import org.softeg.slartus.forpdaplus.core.repositories.QmsThreadsRepository
import org.softeg.slartus.forpdaplus.core_lib.utils.fromHtml
import ru.slartus.feature_qms_contact_threads.fingerprints.QmsThreadItem
import ru.slartus.feature_qms_contact_threads.fingerprints.QmsThreadSelectableItem
import ru.slartus.feature_qms_contact_threads.fingerprints.ThreadItem
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class QmsContactThreadsViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val qmsThreadsRepository: QmsThreadsRepository,
    private val qmsContactsRepository: QmsContactsRepository,
    appPreferences: AppPreferences,

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

    private val _contact = MutableStateFlow<QmsContact?>(null)
    val contact: StateFlow<QmsContact?> = _contact.asStateFlow()

    private val _selectionMode = MutableStateFlow(false)
    val selectionMode: StateFlow<Boolean> = _selectionMode.asStateFlow()

    private var contactId: String? = state[QmsContactThreadsFragment.ARG_CONTACT_ID]
        set(value) {
            field = value
            state[QmsContactThreadsFragment.ARG_CONTACT_ID] = value
        }

    val accentColor: AccentColor = when (appPreferences.accentColor) {
        AppPreferences.ACCENT_COLOR_BLUE_NAME -> AccentColor.Blue
        AppPreferences.ACCENT_COLOR_GRAY_NAME -> AccentColor.Gray
        else -> AccentColor.Pink
    }

    init {
        reload()
        viewModelScope.launch {
            launch(errorHandler) {
                qmsThreadsRepository.threads
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collect { rawItems ->
                        val items = rawItems.map {
                            QmsThreadItem(
                                id = it.id ?: "",
                                title = it.title?.fromHtml()?.toString() ?: "",
                                messagesCount = it.messagesCount ?: 0,
                                newMessagesCount = it.newMessagesCount ?: 0,
                                lastMessageDate = it.lastMessageDate
                            )
                        }
                        _uiState.emit(UiState.Items(items))
                    }
            }
            launch {
                _selectionMode
                    .drop(1)
                    .collect { selectionMode ->
                        val uiState = _uiState.value
                        if (uiState is UiState.Items) {
                            if (selectionMode) {
                                _uiState.emit(uiState.copy(items = uiState.items.map { item ->
                                    QmsThreadSelectableItem(
                                        id = item.id,
                                        title = item.title,
                                        messagesCount = item.messagesCount,
                                        newMessagesCount = item.newMessagesCount,
                                        lastMessageDate = item.lastMessageDate
                                    )
                                }))
                            } else {
                                _uiState.emit(uiState.copy(items = uiState.items.map { item ->
                                    QmsThreadItem(
                                        id = item.id,
                                        title = item.title,
                                        messagesCount = item.messagesCount,
                                        newMessagesCount = item.newMessagesCount,
                                        lastMessageDate = item.lastMessageDate
                                    )
                                }))
                            }
                        }
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
        viewModelScope.launch {
            launch(SupervisorJob() + errorHandler + CoroutineName("qms_threads")) {
                _loading.emit(true)
                try {
                    qmsThreadsRepository.load(contactId)
                } finally {
                    _loading.emit(false)
                }
            }
            launch(SupervisorJob() + errorHandler + CoroutineName("qms_contact")) {
                _contact.emit(qmsContactsRepository.getContact(contactId))
            }
        }
    }

    fun onReloadClick() {
        reload()
    }

    fun onHiddenChanged(hidden: Boolean) {
        if (!hidden)
            reload()
    }

    fun onEventReceived() {
        viewModelScope.launch(errorHandler) {
            _events.emit(Event.Empty)
        }
    }

    fun onThreadClick(item: QmsThreadItem) {
        val contactId = contactId ?: return
        viewModelScope.launch(errorHandler) {
            val contact = _contact.value
            _events.emit(Event.ShowQmsThread(contactId, contact?.nick, item.id, item.title))
        }
    }

    fun onContactProfileClick() {
        val contactId = contactId ?: return
        viewModelScope.launch(errorHandler) {
            val contact = _contact.value
            _events.emit(Event.ShowContactProfile(contactId, contact?.nick))
        }
    }

    fun onNewThreadClick() {
        val contactId = contactId ?: return
        viewModelScope.launch(errorHandler) {
            val contact = _contact.value
            _events.emit(Event.ShowNewThread(contactId, contact?.nick))
        }
    }

    fun onLongClickListener() {
        viewModelScope.launch(errorHandler) {
            _selectionMode.emit(true)
        }
    }

    fun onThreadSelectableClick(item: QmsThreadSelectableItem) {
        Timber.d("onThreadSelectableClick")
        viewModelScope.launch(errorHandler) {
            val uiState = _uiState.value
            if (uiState is UiState.Items) {
                _uiState.emit(uiState.copy(items = uiState.items.map {
                    if (it.id == item.id)
                        QmsThreadSelectableItem(
                            id = it.id,
                            title = it.title,
                            messagesCount = it.messagesCount,
                            newMessagesCount = it.newMessagesCount,
                            lastMessageDate = it.lastMessageDate,
                            selected = !item.selected
                        )
                    else
                        it
                }))
            }
        }
    }

    fun onDeleteSelectionClick() {

        viewModelScope.launch(errorHandler) {
            val uiState = _uiState.value
            val contact = _contact.value?.nick ?: contactId ?: return@launch
            if (uiState is UiState.Items) {
                val selectedIds =
                    uiState.items.filterIsInstance(QmsThreadSelectableItem::class.java)
                        .filter { it.selected }
                        .map { it.id }
                if (selectedIds.isNotEmpty()) {
                    _events.emit(Event.ShowConfirmDeleteDialog(contact, selectedIds))
                }
            }
        }
    }

    fun onConfirmDeleteSelectionClick(selectedIds: List<String>) {
        val contactId = contactId ?: return
        viewModelScope.launch(errorHandler) {
            _events.emit(Event.Progress(true))
            try {
                qmsThreadsRepository.delete(contactId, selectedIds)
            } finally {
                _events.emit(Event.Progress(false))
            }

            _selectionMode.emit(false)

            reload()
        }
    }

    fun onBack(): Boolean {
        val selectionMode = runBlocking {
            _selectionMode.value
        }
        if (selectionMode) {
            viewModelScope.launch(errorHandler) {
                _selectionMode.emit(false)
            }
        }
        return selectionMode
    }

    sealed class UiState {
        object Initialize : UiState()
        data class Items(val items: List<ThreadItem>, val selectionMode: Boolean = false) :
            UiState()
    }

    sealed class Event {
        object Empty : Event()
        data class Progress(val visible: Boolean) : Event()
        data class Error(val exception: Throwable) : Event()

        data class ShowQmsThread(
            val contactId: String,
            val contactNick: String?,
            val threadId: String,
            val threadTitle: String?
        ) : Event()

        data class ShowContactProfile(
            val contactId: String,
            val contactNick: String?
        ) : Event()

        data class ShowNewThread(
            val contactId: String,
            val contactNick: String?
        ) : Event()

        data class ShowConfirmDeleteDialog(
            val userNick: String,
            val selectedIds: List<String>
        ) : Event()
    }

    enum class AccentColor {
        Pink, Blue, Gray
    }
}