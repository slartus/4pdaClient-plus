package org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models

sealed class MessageEvent {
    object ActionInvoked : MessageEvent()
    class MessageTextChanged(val text: String) : MessageEvent()
    class EmoticSelected(val emoticCode: String) : MessageEvent()
    class BbCodeSelected(val bbCodeText: String) : MessageEvent()
}