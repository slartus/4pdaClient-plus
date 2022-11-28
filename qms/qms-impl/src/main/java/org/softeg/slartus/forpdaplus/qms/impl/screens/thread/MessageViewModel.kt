package org.softeg.slartus.forpdaplus.qms.impl.screens.thread

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import org.softeg.slartus.forpdaplus.core_lib.viewmodel.BaseViewModel
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageAction
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageEvent
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageState
import javax.inject.Inject

class MessageViewModel @Inject constructor(
    private val state: SavedStateHandle
) : BaseViewModel<MessageState, MessageAction, MessageEvent>(MessageState()) {
    init {
        Log.e("test1","init:"+ viewState.message)
    }
    override fun obtainEvent(viewEvent: MessageEvent) = when (viewEvent) {
        is MessageEvent.MessageTextChanged -> {
            Log.e("test1", viewState.message)
            viewState = viewState.copy(message = viewEvent.text)
        }
    }
}