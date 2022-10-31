package org.softeg.slartus.forpdaplus.domain_qms

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.domain_qms.db.QmsContactsDao
import org.softeg.slartus.forpdaplus.domain_qms.db.map
import ru.softeg.slartus.qms.api.models.QmsContact
import javax.inject.Inject

class LocalQmsContactsDataSource @Inject constructor(private val qmsContactsDao: QmsContactsDao)  {
    suspend fun getAll(): List<QmsContact> = withContext(Dispatchers.IO) {
        qmsContactsDao.getAll().map { it.map() }
    }

    suspend fun replaceAll(vararg items: QmsContact) = withContext(Dispatchers.IO) {
        qmsContactsDao.deleteAll()
        qmsContactsDao.insertAll(*items.mapIndexedNotNull { index, item -> item.map(index) }
            .toTypedArray())
    }

    suspend fun findById(contactId: String): QmsContact? = withContext(Dispatchers.IO) {
        qmsContactsDao.findById(contactId.toLong())?.map()
    }
}