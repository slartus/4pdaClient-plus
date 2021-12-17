package org.softeg.slartus.forpdaplus.core.services

import org.softeg.slartus.forpdaplus.core.entities.QmsContact

interface QmsService {
    suspend fun getContacts(): List<QmsContact>
    suspend fun deleteContact(contactId: String)
    suspend fun getQmsCount(): Int
}