package ru.softeg.slartus.qms.api.repositories

import kotlinx.coroutines.flow.Flow
import ru.softeg.slartus.qms.api.models.QmsContact

interface QmsContactsRepository {
    val contacts: Flow<List<QmsContact>>
    suspend fun load()
    suspend fun deleteContact(contactId: String)
    suspend fun getContact(contactId: String): QmsContact?
}