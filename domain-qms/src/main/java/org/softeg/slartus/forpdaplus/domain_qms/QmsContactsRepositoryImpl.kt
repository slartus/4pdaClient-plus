package org.softeg.slartus.forpdaplus.domain_qms

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.repositories.QmsContactsRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
import javax.inject.Inject

class QmsContactsRepositoryImpl @Inject constructor(private val qmsService: QmsService) :
    QmsContactsRepository {

    private val _contacts = MutableStateFlow<List<QmsContact>>(emptyList())
    override val contacts
        get() = _contacts.asStateFlow()

    override suspend fun load() {
        _contacts.emit(qmsService.getContacts())
    }
}