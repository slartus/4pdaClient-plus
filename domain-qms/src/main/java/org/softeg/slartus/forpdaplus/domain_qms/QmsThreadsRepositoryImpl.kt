package org.softeg.slartus.forpdaplus.domain_qms

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdaplus.core.entities.QmsThread
import org.softeg.slartus.forpdaplus.core.entities.QmsThreads
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.core.repositories.QmsThreadsRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
import org.softeg.slartus.forpdaplus.domain_qms.di.QmsNewThreadParserString
import javax.inject.Inject

class QmsThreadsRepositoryImpl @Inject constructor(
    private val qmsService: QmsService,
    private val parser: Parser<QmsThreads>,
    @QmsNewThreadParserString private val qmsNewThreadParser: Parser<String>
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