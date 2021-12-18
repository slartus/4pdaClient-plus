package org.softeg.slartus.forpdaplus.domain_qms

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdaplus.core.entities.QmsThread
import org.softeg.slartus.forpdaplus.core.repositories.QmsThreadsRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
import javax.inject.Inject

class QmsThreadsRepositoryImpl @Inject constructor(private val qmsService: QmsService) :
    QmsThreadsRepository {
    private val _threads = MutableStateFlow<List<QmsThread>>(emptyList())
    override val threads
        get() = _threads.asStateFlow()

    override suspend fun load(contactId: String) {
        val items = qmsService.getContactThreads(contactId)
        _threads.emit(items)
    }
}