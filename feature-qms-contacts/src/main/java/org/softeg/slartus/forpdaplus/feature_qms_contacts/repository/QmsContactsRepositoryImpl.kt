package org.softeg.slartus.forpdaplus.feature_qms_contacts.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.repositories.QmsContactsRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
import org.softeg.slartus.forpdaplus.feature_qms_contacts.entities.QmsContactImpl
import javax.inject.Inject

class QmsContactsRepositoryImpl @Inject constructor(private val qmsService: QmsService) :
    QmsContactsRepository {
    private val _contacts = MutableStateFlow<List<QmsContact>>(emptyList())
    override val contacts: StateFlow<List<QmsContact>>
        get() = _contacts.asStateFlow()

    override suspend fun load() {
        val newList = qmsService.getContacts()
            .map { QmsContactImpl(it.id, it.nick, it.avatarUrl, it.newMessagesCount) }
        _contacts.emit(newList)
    }
}