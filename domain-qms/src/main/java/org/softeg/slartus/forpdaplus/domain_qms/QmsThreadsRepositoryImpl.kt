package org.softeg.slartus.forpdaplus.domain_qms

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdaplus.core.entities.QmsThread
import org.softeg.slartus.forpdaplus.core.entities.QmsThreads
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.core.repositories.QmsThreadsRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
import javax.inject.Inject

class QmsThreadsRepositoryImpl @Inject constructor(
    private val qmsService: QmsService,
    private val parser: Parser<QmsThreads>
) :
    QmsThreadsRepository {
    private val _threads = MutableStateFlow<List<QmsThread>>(emptyList())
    override val threads
        get() = _threads.asStateFlow()

    override suspend fun load(contactId: String) {
        val items = qmsService.getContactThreads(contactId, parser.id)
        _threads.emit(items)
    }
}