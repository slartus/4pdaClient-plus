package org.softeg.slartus.forpdaplus.domain_qms

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.softeg.slartus.qms.api.models.QmsThread
import ru.softeg.slartus.qms.api.models.QmsThreads
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import ru.softeg.slartus.qms.api.repositories.QmsThreadsRepository
import ru.softeg.slartus.qms.api.QmsService
import javax.inject.Inject

class QmsThreadsRepositoryImpl @Inject constructor(
    private val qmsService: QmsService,
    private val parser: Parser<QmsThreads>,
    private val qmsNewThreadParser: Parser<String>
) :
    QmsThreadsRepository {
    private val _threads = MutableStateFlow<List<QmsThread>?>(null)
    override val threads
        get() = _threads.asStateFlow()

    override suspend fun load(contactId: String) {
        val items = qmsService.getContactThreads(contactId, parser.id)
        _threads.emit(items)
    }

    override suspend fun delete(contactId: String, threadIds: List<String>) {
        qmsService.deleteThreads(contactId, threadIds)
    }

    override suspend fun createNewThread(
        contactId: String,
        userNick: String,
        subject: String,
        message: String
    ) = qmsService.createNewThread(contactId, userNick, subject, message, qmsNewThreadParser.id)
}