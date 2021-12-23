package org.softeg.slartus.forpdaplus.core_db.qms_contacts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.db.QmsContactsTable
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import javax.inject.Inject
import org.softeg.slartus.forpdaplus.core_db.qms_contacts.QmsContact as QmsContactDb

class QmsContactsTableImpl @Inject constructor(private val qmsContactsDao: QmsContactsDao) :
    QmsContactsTable {
    override suspend fun getAll(): List<QmsContact> = withContext(Dispatchers.IO) {
        qmsContactsDao.getAll().map { it.map() }
    }

    override suspend fun insertAll(vararg items: QmsContact) = withContext(Dispatchers.IO) {
        qmsContactsDao.deleteAll()
        qmsContactsDao.insertAll(*items.mapIndexedNotNull { index, item -> item.map(index) }
            .toTypedArray())
    }

    override suspend fun findById(contactId: String): QmsContact? = withContext(Dispatchers.IO) {
        qmsContactsDao.findById(contactId.toLong())?.map()
    }

    private data class QmsContactImpl(
        override val id: String?,
        override val nick: String?,
        override val avatarUrl: String?,
        override val newMessagesCount: Int?
    ) : QmsContact

    companion object {
        private fun QmsContactDb.map(): QmsContact =
            QmsContactImpl(this.id.toString(), this.nick, this.avatarUrl, this.messagesCount)

        private fun QmsContact.map(sort: Int?): QmsContactDb? = this.id?.let { id ->
            QmsContactDb(id.toLong(), this.nick, this.avatarUrl, this.newMessagesCount, sort)
        }
    }
}