package org.softeg.slartus.forpdaplus.feature_qms_contacts.ui

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
import org.softeg.slartus.forpdaplus.core.repositories.QmsContactsRepository
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.feature_qms_contacts.ui.fingerprints.QmsContactItem
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
        "blue" -> AccentColor.Blue
        "gray" -> AccentColor.Gray
        else -> AccentColor.Standart
    }

    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        _events.value = Event.Error(ex)
    }

    private val _events = MutableStateFlow<Event>(Event.Empty)
    val events: StateFlow<Event> = _events.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Initialize)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
                                id = it.id ?: "",
                                nick = it.nick ?: "",
                                avatarUrl = it.avatarUrl,
                                newMessagesCount = it.newMessagesCount ?: 0
                            )
                        }

                        _uiState.emit(UiState.Items(contacts))
                    }
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

    private fun reload() {
        viewModelScope.launch(errorHandler) {
            _loading.emit(true)
            qmsContactsRepository.load()
            _loading.emit(false)
        }
    }

    fun onContactClick(item: QmsContactItem) {
        viewModelScope.launch(errorHandler) {
            _events.emit(Event.OpenContactThreads(item.id, item.nick))
        }
    }

    fun onContactLongClick(item: QmsContactItem) {
        // TODO("Not yet implemented")
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

        data class OpenContactThreads(val contactId: String, val contactNick: String?) : Event()
    }

    enum class AccentColor {
        Standart, Blue, Gray
    }
}