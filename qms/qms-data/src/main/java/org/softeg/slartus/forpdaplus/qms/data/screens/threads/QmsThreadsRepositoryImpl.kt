package org.softeg.slartus.forpdaplus.qms.data.screens.threads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.softeg.slartus.qms.api.models.QmsThread
import ru.softeg.slartus.qms.api.repositories.QmsThreadsRepository
import ru.softeg.slartus.qms.api.QmsService
import javax.inject.Inject

class QmsThreadsRepositoryImpl @Inject constructor(
    private val qmsService: QmsService
) :
    QmsThreadsRepository {
    private val _threads = MutableStateFlow<List<QmsThread>?>(null)
    override val threads
        get() = _threads.asStateFlow()

    override suspend fun load(contactId: String) {
        val items = qmsService.getContactThreads(contactId)
        _threads.emit(items)
    }

    override suspend fun delete(contactId: String, threadIds: List<String>) {
        qmsService.deleteThreads(contactId, threadIds)
    }
}