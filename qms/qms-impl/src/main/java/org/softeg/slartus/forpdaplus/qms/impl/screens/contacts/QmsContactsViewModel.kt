package org.softeg.slartus.forpdaplus.qms.impl.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.softeg.slartus.forpdaplus.core.AppPreferences
import org.softeg.slartus.forpdaplus.core.QmsPreferences
import ru.softeg.slartus.qms.api.repositories.QmsContactsRepository
import org.softeg.slartus.forpdaplus.qms.impl.R

import org.softeg.slartus.forpdaplus.qms.impl.screens.contacts.fingerprints.QmsContactItem
import ru.softeg.slartus.common.api.AppAccentColor
import ru.softeg.slartus.common.api.AppTheme
import javax.inject.Inject

@HiltViewModel
class QmsContactsViewModel @Inject constructor(
    private val qmsContactsRepository: QmsContactsRepository,
    appTheme: AppTheme,
    qmsPreferences: QmsPreferences
) : ViewModel() {

    val showAvatars: Boolean = qmsPreferences.showAvatars
    val squareAvatars: Boolean = qmsPreferences.squareAvatars
    val accentColor: AppAccentColor = runBlocking { appTheme.getAccentColor()} // TODO: убрать runBlocking

    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        _events.value = QmsContactsAction.Error(ex)
    }

    private val _events = MutableStateFlow<QmsContactsAction>(QmsContactsAction.Empty)
    val events: StateFlow<QmsContactsAction> = _events.asStateFlow()

    private val _uiState = MutableStateFlow(QmsContactsState())
    val uiState: StateFlow<QmsContactsState> = _uiState.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        reload()
        viewModelScope.launch {
            launch(errorHandler) {
                qmsContactsRepository.contacts
                    .distinctUntilChanged()
                    .collect { rawItems ->
                        val contacts = rawItems.map {
                            QmsContactItem(
                                id = it.id,
                                nick = it.nick,
                                avatarUrl = it.avatarUrl,
                                newMessagesCount = it.newMessagesCount
                            )
                        }

                        _uiState.emit(_uiState.value.copy(items = contacts, filteredItems = contacts))
                    }
            }
        }
    }

    fun onReloadClick() {
        reload()
    }

    fun onEventReceived() {
        viewModelScope.launch(errorHandler) {
            _events.emit(QmsContactsAction.Empty)
        }
    }

    private fun reload() {
        viewModelScope.launch(errorHandler) {
            _loading.emit(true)
            try {
                qmsContactsRepository.load()
            } finally {
                _loading.emit(false)
            }
        }
    }

    fun onContactDeleteClick(item: QmsContactItem) {
        viewModelScope.launch(errorHandler) {
            try {
                qmsContactsRepository.deleteContact(item.id)
                _events.emit(QmsContactsAction.ShowToast(R.string.contact_deleted))
            } finally {
                reload()
            }
        }
    }

    fun obtainEvent(event: QmsContactsEvent) = when (event) {
        is QmsContactsEvent.HiddenChanged -> onHiddenChanged(event.hidden)

        is QmsContactsEvent.OnSearchTextChanged -> handleOnSearchTextChanged(event.text)
    }

    private fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            reload()
        }
    }

    private fun handleOnSearchTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(
            filteredItems = _uiState.value.items.filter { text.isEmpty() || it.nick.contains(text, ignoreCase = true) }
        )
    }
}