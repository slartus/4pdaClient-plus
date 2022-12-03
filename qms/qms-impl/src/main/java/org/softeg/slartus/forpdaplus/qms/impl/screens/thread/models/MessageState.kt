package org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models

data class MessageState(
    val message: String = "",
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0
) {
    val sendButtonVisible: Boolean = message.isNotEmpty()
}