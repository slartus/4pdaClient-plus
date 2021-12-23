package org.softeg.slartus.forpdaplus.core.repositories

import kotlinx.coroutines.flow.Flow
import org.softeg.slartus.forpdaplus.core.entities.QmsContact

interface QmsContactsRepository {
    val contacts: Flow<List<QmsContact>>
    suspend fun load()
    suspend fun deleteContact(contactId: String)
    suspend fun getContact(contactId: String): QmsContact?
}