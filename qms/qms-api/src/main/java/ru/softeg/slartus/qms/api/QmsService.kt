package ru.softeg.slartus.qms.api

import ru.softeg.slartus.qms.api.models.QmsContact
import ru.softeg.slartus.qms.api.models.QmsThread

interface QmsService {
    suspend fun getContacts(resultParserId: String): List<QmsContact>
    suspend fun deleteContact(contactId: String)
    suspend fun getQmsCount(resultParserId: String): Int
    suspend fun getContactThreads(
        contactId: String,
        resultParserId: String? = null
    ): List<QmsThread>

    suspend fun getContact(contactId: String, resultParserId: String? = null): QmsContact?
    suspend fun deleteThreads(contactId: String, threadIds: List<String>)
    suspend fun createNewThread(
        contactId: String,
        userNick: String,
        subject: String,
        message: String,
        resultParserId: String? = null
    ): String?

    companion object {
        const val ARG_CONTACT_ID = "QmsService.CONTACT_ID"
    }
}