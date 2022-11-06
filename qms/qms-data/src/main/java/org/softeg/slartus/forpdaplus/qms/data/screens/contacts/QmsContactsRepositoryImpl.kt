package org.softeg.slartus.forpdaplus.qms.data.screens.contacts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.softeg.slartus.qms.api.models.QmsContact
import ru.softeg.slartus.qms.api.repositories.QmsContactsRepository
import ru.softeg.slartus.qms.api.QmsService
import javax.inject.Inject

class QmsContactsRepositoryImpl @Inject constructor(
    private val qmsService: QmsService,
    private val qmsContactsTable: LocalQmsContactsDataSource
) :
    QmsContactsRepository {

    private val _contacts = MutableStateFlow<List<QmsContact>>(emptyList())
    override val contacts
        get() = _contacts.asStateFlow()

    override suspend fun load() {
        _contacts.emit(qmsContactsTable.getAll())
        val contacts = qmsService.getContacts()

        qmsContactsTable.replaceAll(*contacts.toTypedArray())
        _contacts.emit(qmsContactsTable.getAll())
    }

    override suspend fun deleteContact(contactId: String) {
        qmsService.deleteContact(contactId)
    }

    override suspend fun getContact(contactId: String): QmsContact? {
        return qmsContactsTable.findById(contactId) ?: qmsService.getContact(contactId)
    }
}