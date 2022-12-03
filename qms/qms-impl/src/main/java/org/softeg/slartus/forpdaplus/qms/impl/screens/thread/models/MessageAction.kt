package org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models

sealed class MessageAction {
    class InsertText(val text: String) : MessageAction()
}