package org.softeg.slartus.forpdaplus.core.entities

interface QmsThread {
    val id: String?
    val title: String?
    val messagesCount: Int?
    val newMessagesCount: Int?
    val lastMessageDate: String?
}

class QmsThreads(list: List<QmsThread>) : List<QmsThread> by list