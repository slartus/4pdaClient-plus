package ru.softeg.slartus.qms.api

import ru.softeg.slartus.qms.api.models.QmsContact
import ru.softeg.slartus.qms.api.models.QmsThread

interface QmsService {
    suspend fun getContacts(resultParserId: String): List<QmsContact>
    suspend fun deleteContact(contactId: String)
    suspend fun getQmsCount(resultParserId: String): Int
    suspend fun getContactThreads(contactId: String, resultParserId: String): List<QmsThread>
    suspend fun getContact(contactId: String, resultParserId: String): QmsContact?
    suspend fun deleteThreads(contactId: String, threadIds: List<String>)

    companion object {
        const val ARG_CONTACT_ID = "QmsService.CONTACT_ID"
    }
}