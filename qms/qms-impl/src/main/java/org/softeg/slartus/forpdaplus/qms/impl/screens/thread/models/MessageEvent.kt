package org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models

sealed class MessageEvent {
    class MessageTextChanged(val text: String) : MessageEvent()
}