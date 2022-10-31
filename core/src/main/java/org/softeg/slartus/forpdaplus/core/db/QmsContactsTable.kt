package org.softeg.slartus.forpdaplus.core.db

import ru.softeg.slartus.qms.api.models.QmsContact

interface QmsContactsTable {
    suspend fun getAll(): List<QmsContact>
    suspend fun insertAll(vararg items: QmsContact)
    suspend fun findById(contactId: String): QmsContact?
}