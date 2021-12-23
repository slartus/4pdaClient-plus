package org.softeg.slartus.forpdaplus.domain_qms

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdaplus.core.db.QmsContactsTable
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.entities.QmsContacts
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.core.repositories.QmsContactsRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
import javax.inject.Inject

class QmsContactsRepositoryImpl @Inject constructor(
    private val qmsService: QmsService,
    private val qmsContactsTable: QmsContactsTable,
    private val parser: Parser<QmsContacts>
) :
    QmsContactsRepository {

    private val _contacts = MutableStateFlow<List<QmsContact>>(emptyList())
    override val contacts
        get() = _contacts.asStateFlow()

    override suspend fun load() {
        _contacts.emit(qmsContactsTable.getAll())
        val contacts = qmsService.getContacts(parser.id)

        qmsContactsTable.insertAll(*contacts.toTypedArray())
        _contacts.emit(qmsContactsTable.getAll())
    }

    override suspend fun deleteContact(contactId: String) {
        qmsService.deleteContact(contactId)
    }

    override suspend fun getContact(contactId: String): QmsContact? {
        return qmsContactsTable.findById(contactId) ?: qmsService.getContact(contactId, parser.id)
    }
}