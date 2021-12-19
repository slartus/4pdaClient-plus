package org.softeg.slartus.forpdaplus.core.services

import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.entities.QmsThread

interface QmsService {
    suspend fun getContacts(): List<QmsContact>
    suspend fun deleteContact(contactId: String)
    suspend fun getQmsCount(): Int
    suspend fun getContactThreads(contactId: String): List<QmsThread>
    suspend fun getContact(contactId: String): QmsContact?
}