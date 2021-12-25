package org.softeg.slartus.forpdaplus.core.repositories

import kotlinx.coroutines.flow.Flow
import org.softeg.slartus.forpdaplus.core.entities.QmsThread

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