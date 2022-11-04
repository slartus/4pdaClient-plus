package org.softeg.slartus.forpdaplus.qms.impl.screens.contacts

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.AppPreferences
import org.softeg.slartus.forpdaplus.core.QmsPreferences
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import ru.softeg.slartus.qms.api.repositories.QmsContactsRepository
import org.softeg.slartus.forpdaplus.qms.impl.R

import org.softeg.slartus.forpdaplus.qms.impl.screens.contacts.fingerprints.QmsContactItem
import javax.inject.Inject

@HiltViewModel
class QmsContactsViewModel @Inject constructor(
    private val qmsContactsRepository: QmsContactsRepository,
    appPreferences: AppPreferences,
    qmsPreferences: QmsPreferences
) : ViewModel() {

    val showAvatars: Boolean = qmsPreferences.showAvatars
    val squareAvatars: Boolean = qmsPreferences.squareAvatars
    val accentColor: AccentColor = when (appPreferences.accentColor) {
        AppPreferences.ACCENT_COLOR_BLUE_NAME -> AccentColor.Blue
        AppPreferences.ACCENT_COLOR_GRAY_NAME -> AccentColor.Gray
        else -> AccentColor.Pink
    }

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

    enum class AccentColor {
        Pink, Blue, Gray
    }
    private fun handleOnSearchTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(
            filteredItems = _uiState.value.items.filter { text.isEmpty() || it.nick.contains(text, ignoreCase = true) }
        )
    }
}