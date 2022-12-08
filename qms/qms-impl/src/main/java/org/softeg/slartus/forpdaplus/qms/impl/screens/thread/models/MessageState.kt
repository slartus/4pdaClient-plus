package org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models

data class MessageState(
    val message: String = "",
) {
    val sendButtonVisible: Boolean = message.isNotEmpty()
}