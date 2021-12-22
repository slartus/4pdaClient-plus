package org.softeg.slartus.forpdaplus.core.entities

interface QmsContact {
    val id: String?
    val nick: String?
    val avatarUrl: String?
    val newMessagesCount: Int?
}

class QmsContacts(list: List<QmsContact>) : List<QmsContact> by list