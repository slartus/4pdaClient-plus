package org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models

sealed class MessageAction {
    class SendMessage(val text: String) : MessageAction()
    class InsertText(val text: String) : MessageAction()
}