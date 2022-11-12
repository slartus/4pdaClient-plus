package ru.softeg.slartus.qms.api.repositories

import kotlinx.coroutines.flow.Flow
import ru.softeg.slartus.qms.api.models.QmsThread

interface QmsThreadsRepository {
    val threads: Flow<List<QmsThread>?>
    suspend fun load(contactId: String)
    suspend fun delete(contactId: String, threadIds: List<String>)
    suspend fun createNewThread(
        contactId: String,
        userNick: String,
        subject: String,
        message: String
    ): String?
}