package org.softeg.slartus.forpdaplus.qms.data.screens.threads

import ru.softeg.slartus.qms.api.QmsService
import ru.softeg.slartus.qms.api.models.QmsThread
import ru.softeg.slartus.qms.api.repositories.QmsThreadsRepository
import javax.inject.Inject

class QmsThreadsRepositoryImpl @Inject constructor(
    private val qmsService: QmsService
) :
    QmsThreadsRepository {

    override suspend fun load(contactId: String): List<QmsThread> {
        return qmsService.getContactThreads(contactId)
    }

    override suspend fun delete(contactId: String, threadIds: List<String>) {
        qmsService.deleteThreads(contactId, threadIds)
    }
}