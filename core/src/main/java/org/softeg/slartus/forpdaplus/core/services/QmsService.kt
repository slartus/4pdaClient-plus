package org.softeg.slartus.forpdaplus.core.services

import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.entities.QmsThread

interface QmsService {
    suspend fun getContacts(resultParserId: String): List<QmsContact>
    suspend fun deleteContact(contactId: String)
    suspend fun getQmsCount(resultParserId: String): Int
    suspend fun getContactThreads(contactId: String, resultParserId: String): List<QmsThread>
    suspend fun getContact(contactId: String, resultParserId: String): QmsContact?

    companion object {
        const val ARG_CONTACT_ID = "QmsService.CONTACT_ID"
    }
}