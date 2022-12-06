package org.softeg.slartus.forpdaplus.qms.impl.screens.thread

import dagger.hilt.android.lifecycle.HiltViewModel
import org.softeg.slartus.forpdaplus.core_lib.viewmodel.BaseViewModel
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageAction
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageEvent
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageState
import ru.softeg.slartus.common.api.repositories.EmoticsRepository
import javax.inject.Inject


@HiltViewModel
class MessageViewModel @Inject constructor(
) : BaseViewModel<MessageState, MessageAction, MessageEvent>(MessageState()) {

    override fun obtainEvent(viewEvent: MessageEvent) = when (viewEvent) {
        MessageEvent.ActionInvoked -> viewAction = null

        is MessageEvent.MessageTextChanged ->
            viewState = viewState.copy(message = viewEvent.text)

        is MessageEvent.EmoticSelected -> viewAction =
            MessageAction.InsertText(viewEvent.emoticCode)
        is MessageEvent.BbCodeSelected -> viewAction =
            MessageAction.InsertText(viewEvent.bbCodeText)
        MessageEvent.SendClicked -> viewAction = MessageAction.SendMessage(viewState.message)
        MessageEvent.ClearRequest -> viewState = viewState.copy(message = "")
    }
}
