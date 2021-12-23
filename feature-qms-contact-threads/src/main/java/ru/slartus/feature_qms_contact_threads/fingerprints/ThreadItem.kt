package ru.slartus.feature_qms_contact_threads.fingerprints

import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item

interface ThreadItem : Item {
    val id: String
    val title: String
    val messagesCount: Int
    val newMessagesCount: Int
    val lastMessageDate: String?
}