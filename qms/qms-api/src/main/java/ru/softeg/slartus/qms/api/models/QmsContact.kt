package ru.softeg.slartus.qms.api.models

data class QmsContact(
    val id: String,
    val nick: String,
    val avatarUrl: String?,
    val newMessagesCount: Int
)


class QmsContacts(list: List<QmsContact>) : List<QmsContact> by list